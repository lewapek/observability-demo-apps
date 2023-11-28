package pl.lewapek.workshop.observability.metrics

import io.opentelemetry.api.trace.SpanKind
import pl.lewapek.workshop.observability.config.VariantConfig
import zio.http.{Headers, Request, Response}
import zio.telemetry.opentelemetry.baggage.Baggage
import zio.telemetry.opentelemetry.context.{IncomingContextCarrier, OutgoingContextCarrier}
import zio.telemetry.opentelemetry.tracing.Tracing
import zio.telemetry.opentelemetry.tracing.propagation.TraceContextPropagator
import zio.{ZIO, ZLayer}

import scala.collection.mutable

class TracingService(val tracing: Tracing, val baggage: Baggage, variantConfig: VariantConfig):
  import TracingService.*
  def withTracing[R, E](request: Request, spanName: String)(zio: ZIO[R, E, Response]): ZIO[R, E, Response] =
    val inputCarrier = incomingHeadersCarrier(request.headers)
    (tracing.setAttribute("app-variant", variantConfig.version.toString) *>
      tracing.setAttribute("namespace", variantConfig.namespace) *>
      zio) @@
      tracing.aspects.extractSpan(TraceContextPropagator.default, inputCarrier, spanName, SpanKind.SERVER) @@
      PrometheusMetrics.requestHandlerTimer.tagged("endpoint", spanName).trackDuration
  end withTracing

  def withTracingCarriers[R, E](request: Request, spanName: String)(
    zio: Carriers => ZIO[R, E, Response]
  ): ZIO[R, E, Response] =
    val carriers = Carriers(
      incomingHeadersCarrier(request.headers),
      OutgoingContextCarrier.default()
    )
    (
      tracing.inject(TraceContextPropagator.default, carriers.output) *>
        tracing.setAttribute("app-variant", variantConfig.version.toString) *>
        tracing.setAttribute("namespace", variantConfig.namespace) *>
        zio(carriers) <*
        ZIO.logInfo(s"Headers: ${carriers.outputHeaders}")
    ) @@ tracing.aspects.extractSpan(TraceContextPropagator.default, carriers.input, spanName, SpanKind.SERVER) @@
      PrometheusMetrics.requestHandlerTimer.tagged("endpoint", spanName).trackDuration

  end withTracingCarriers
end TracingService

object TracingService:
  final case class TracingBaggage(tracing: Tracing, baggage: Baggage)
  final case class TracingHeaders(value: Map[String, String])
  final case class Carriers(
    input: IncomingContextCarrier[Headers],
    output: OutgoingContextCarrier[mutable.Map[String, String]]
  ):
    def outputHeaders: TracingHeaders = TracingHeaders(output.kernel.toMap)
  end Carriers

  private def incomingHeadersCarrier(initial: Headers): IncomingContextCarrier[Headers] =
    new IncomingContextCarrier[Headers]:
      override val kernel: Headers = Headers(initial)

      override def getAllKeys(carrier: Headers): Iterable[String] =
        carrier.headers.headers.map(_.headerName)

      override def getByKey(carrier: Headers, key: String): Option[String] =
        carrier.headers.get(key)
    end new
  end incomingHeadersCarrier

  val layer = ZLayer.fromFunction(TracingService(_, _, _))
end TracingService
