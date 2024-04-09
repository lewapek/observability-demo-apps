package pl.lewapek.workshop.observability.http

import pl.lewapek.workshop.observability.metrics.TracingService.*
import pl.lewapek.workshop.observability.metrics.{PrometheusMetrics, TracingService}
import pl.lewapek.workshop.observability.service.ForwardingService
import pl.lewapek.workshop.observability.service.ForwardingService.ForwardRequestInput
import zio.*
import zio.http.*
import zio.http.Method.POST
import zio.json.*
import zio.json.ast.Json
import zio.metrics.MetricLabel
import zio.telemetry.opentelemetry.baggage.propagation.BaggagePropagator
import zio.telemetry.opentelemetry.tracing.propagation.TraceContextPropagator

object CommonRoutes:
  def make(tracingService: TracingService) =
    import tracingService.*
    Http.collectZIO[Request] {
      case request @ POST -> !! / "common" / "print" =>
        withTracing(request, "/common/print") {
          for
            bodyString <- request.body.asString
            _          <- ZIO.logInfo(s"Got body: ${bodyString}")
          yield Response.ok
        }
      case request @ POST -> !! / "common" / "print-json" =>
        withTracing(request, "/common/print-json") {
          for
            bodyString <- request.body.jsonAs[Json]
            _          <- ZIO.logInfo(s"Got json body: ${bodyString.toJson}")
          yield Response.ok
        }
      case request @ POST -> !! / "common" / "forward" =>
        println("got request")
        withTracingCarriers(request, "forward") { case Carriers(inputCarrier, outputCarrier) =>
          for
            _ <- ZIO.debug("inside for")
            forwardingInput <- request.body.jsonAs[ForwardRequestInput]
            _ <- ZIO.debug(s"got input $forwardingInput")
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
            _ <- ZIO.debug(s"before forward call")
            response <- ForwardingService.forward(
              forwardingInput.withHeaders(outputCarrier.kernel.toMap)
            )
            _ <- ZIO.debug(s"after forward call $response")
            _ <- tracing.addEvent("after forwarding")
            _ <- tracing.setAttribute("ttl", forwardingInput.ttl)
          yield Response.json(response.toJson)
          end for
        }
      case request @ POST -> !! / "common" / "async-job" =>
        withTracing(request, "/common/async/job") {
          for
            id        <- Random.nextUUID
            sleepTime <- Random.nextIntBetween(30, 120).map(_.seconds)
            durationType = if sleepTime.toSeconds <= 60 then JobDuration.Short else JobDuration.Long
            durationLabel  = MetricLabel("job_duration", durationType.toString.toLowerCase())
            _ <- ZIO
              .logAnnotate(LogAnnotation("jobId", id.toString))(
                ZIO.scoped(
                  ZIO.acquireRelease(
                    for
                      _ <- ZIO.logInfo("Starting job")
                      _ <- PrometheusMetrics.asyncJobsInProgress.tagged(durationLabel).increment
                      _ <- ZIO.sleep(sleepTime)
                    yield ()
                  )(_ =>
                    ZIO.logInfo("Finished job") *>
                      PrometheusMetrics.asyncJobsInProgress.tagged(durationLabel).decrement *>
                      JobStatus.random
                        .flatMap { status =>
                          PrometheusMetrics.asyncJobsFinished
                            .tagged(durationLabel, MetricLabel("job_status", status.toString))
                            .increment
                        }
                  )
                )
              )
              .forkDaemon
          yield Response.json(id.toJson)
        }
      case request @ POST -> !! / "common" / "sleep" / int(millis) =>
        withTracing(request, "/common/sleep/<sec>") {
          ZIO.succeed(Response.ok).delay(millis.millis)
        }
    }
  end make

end CommonRoutes

enum JobDuration:
  case Short, Long
end JobDuration

enum JobStatus:
  case Succeeded, Failed
end JobStatus

object JobStatus:
  def random: UIO[JobStatus] =
    Random.nextIntBounded(10).map(int => if int < 3 then JobStatus.Failed else JobStatus.Succeeded)
end JobStatus
