package pl.lewapek.workshop.observability.http

import pl.lewapek.workshop.observability.metrics.AppTracing.*
import pl.lewapek.workshop.observability.metrics.{AppTracing, PrometheusMetrics}
import pl.lewapek.workshop.observability.service.ForwardingService
import pl.lewapek.workshop.observability.service.ForwardingService.ForwardRequestInput
import zio.*
import zio.http.*
import zio.http.Method.POST
import zio.json.*
import zio.telemetry.opentelemetry.baggage.Baggage
import zio.telemetry.opentelemetry.baggage.propagation.BaggagePropagator
import zio.telemetry.opentelemetry.tracing.Tracing
import zio.telemetry.opentelemetry.tracing.propagation.TraceContextPropagator

object CommonRoutes:
  def make(tracing: Tracing, baggage: Baggage) =
    given Tracing = tracing
    given Baggage = baggage

    Http.collectZIO[Request] {
      case request @ POST -> !! / "common" / "forward" =>
        withTracingCarriers(request, "forward") { case Carriers(inputCarrier, outputCarrier) =>
          for
            forwardingInput <- request.body.jsonAs[ForwardRequestInput]
            _ <- ZIO.when(forwardingInput.ttl == 2)(
              baggage.set("baggage-key", "baggage-value") *>
                tracing.setAttribute("ttl<=2", "yes")
            )
            _ <- ZIO.when(forwardingInput.ttl != 3)(
              baggage.extract(BaggagePropagator.default, inputCarrier) *> baggage
                .get("baggage-key")
                .flatMap(maybeValue => tracing.setAttribute("baggage-key", maybeValue.getOrElse("no-baggage")))
            )
            _ <- baggage.inject(BaggagePropagator.default, outputCarrier)
            _ <- tracing.inject(TraceContextPropagator.default, outputCarrier)
            _ <- tracing.addEvent("before forwarding")
            response <- ForwardingService.forward(
              forwardingInput.withHeaders(outputCarrier.kernel.toMap)
            )
            _ <- tracing.addEvent("after forwarding")
            _ <- tracing.setAttribute("ttl", forwardingInput.ttl)
          yield Response.json(response.toJson)
          end for
        }
      case request @ POST -> !! / "common" / "async-job" =>
        withTracing(request, "/common/async/job") {
          for
            id        <- Random.nextUUID
            sleepTime <- Random.nextIntBetween(30, 60).map(_.seconds)
            _ <- ZIO
              .logAnnotate(LogAnnotation("jobId", id.toString))(
                ZIO.scoped(
                  ZIO.acquireRelease(
                    for
                      _ <- ZIO.logInfo("Starting job")
                      _ <- PrometheusMetrics.asyncJobsInProgress.increment
                      _ <- ZIO.sleep(sleepTime)
                    yield ()
                  )(_ => ZIO.logInfo("Finished job") *> PrometheusMetrics.asyncJobsInProgress.decrement)
                )
              )
              .forkDaemon
          yield Response.json(id.toJson)
        }
    }
  end make

end CommonRoutes
