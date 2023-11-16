package pl.lewapek.products.http

import io.opentelemetry.api.trace.SpanKind
import pl.lewapek.products.metrics.PrometheusMetrics
import pl.lewapek.products.service.ForwardingService
import pl.lewapek.products.service.ForwardingService.ForwardRequestInput
import zio.*
import zio.http.*
import zio.http.Method.POST
import zio.json.*
import zio.telemetry.opentelemetry.baggage.Baggage
import zio.telemetry.opentelemetry.baggage.propagation.BaggagePropagator
import zio.telemetry.opentelemetry.context.{IncomingContextCarrier, OutgoingContextCarrier}
import zio.telemetry.opentelemetry.tracing.Tracing
import zio.telemetry.opentelemetry.tracing.propagation.TraceContextPropagator

object AppRoutes:
  def make(tracing: Tracing, baggage: Baggage) =
    import tracing.aspects.*
    Http.collectZIO[Request] {
      case request @ POST -> !! / "app" / "forward" =>
        val inCarrier  = incomingHeadersCarrier(request.headers)
        val outCarrier = OutgoingContextCarrier.default()

        (
          for
            stringBody      <- request.body.asString
            forwardingInput <- ZIO.fromEither(stringBody.fromJson[ForwardRequestInput])

            _ <- ZIO.foreachDiscard(request.headers.filter(h => Set("baggage", "traceparent").contains(h.headerName)))(
              h => ZIO.debug(s"  $h")
            )
            _ <- ZIO.when(forwardingInput.ttl == 3)(
              baggage.set("woj", "mytyt") *> tracing.setAttribute("abc", forwardingInput.ttl)
            )
            _ <- ZIO.when(forwardingInput.ttl != 3)(
              baggage.extract(BaggagePropagator.default, inCarrier) *> baggage
                .get("woj")
                .flatMap(maybeString => tracing.setAttribute("woj", maybeString.getOrElse("OJOJ")))
            )
            _ <- baggage.inject(BaggagePropagator.default, outCarrier)
            _ <- tracing.inject(TraceContextPropagator.default, outCarrier)

            _ <- tracing.addEvent("event from backend before response")
            _ <- ZIO.debug(s"OUT CARRIER: ${outCarrier.kernel.toMap}")
            response <- ForwardingService.forward(
              forwardingInput.withHeaders(outCarrier.kernel.toMap)
            )
            _ <- tracing.addEvent("event from backend after response")
            _ <- tracing.setAttribute("ttl", forwardingInput.ttl)
          yield Response.json(response.toJson)
        ) @@ extractSpan(TraceContextPropagator.default, inCarrier, "/app/forward", SpanKind.SERVER)
      case request @ POST -> !! / "app" / "report" =>
        for
          id        <- Random.nextUUID
          sleepTime <- Random.nextIntBetween(30, 60).map(_.seconds)
          _ <- ZIO
            .logAnnotate(LogAnnotation("reportId", id.toString))(
              ZIO.scoped(
                ZIO.acquireRelease(
                  ZIO.logInfo("Starting report") *>
                    PrometheusMetrics.reportsInProgress.increment *>
                    ZIO.sleep(sleepTime)
                )(_ => ZIO.logInfo("Finished report") *> PrometheusMetrics.reportsInProgress.decrement)
              )
            )
            .forkDaemon
        yield Response.json(id.toJson)
    }
  end make

  def incomingHeadersCarrier(initial: Headers): IncomingContextCarrier[Headers] =
    new IncomingContextCarrier[Headers]:
      override val kernel: Headers = Headers(initial)
      override def getAllKeys(carrier: Headers): Iterable[String] =
        carrier.headers.headers.map(_.headerName)
      override def getByKey(carrier: Headers, key: String): Option[String] =
        carrier.headers.get(key)
    end new
  end incomingHeadersCarrier
end AppRoutes
