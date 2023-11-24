package pl.lewapek.workshop.observability.metrics

import io.opentelemetry.api.trace.SpanKind
import zio.ZIO
import zio.http.{Headers, Request, Response}
import zio.telemetry.opentelemetry.baggage.Baggage
import zio.telemetry.opentelemetry.context.{IncomingContextCarrier, OutgoingContextCarrier}
import zio.telemetry.opentelemetry.tracing.Tracing
import zio.telemetry.opentelemetry.tracing.propagation.TraceContextPropagator

import scala.collection.mutable

object AppTracing:
  val tracing                                                         = ZIO.service[Tracing]
  val baggage                                                         = ZIO.service[Baggage]
  val tracingBaggage: ZIO[Tracing & Baggage, Nothing, TracingBaggage] = tracing.zipWith(baggage)(TracingBaggage.apply)

  def withTracing[R, E](request: Request, spanName: String)(zio: ZIO[R, E, Response])(using
    tracing: Tracing
  ): ZIO[R, E, Response] =
    val inputCarrier = incomingHeadersCarrier(request.headers)
    zio @@ tracing.aspects.extractSpan(TraceContextPropagator.default, inputCarrier, spanName, SpanKind.SERVER) @@
      PrometheusMetrics.requestHandlerTimer.tagged("endpoint", spanName).trackDuration
  end withTracing

  def withTracingCarriers[R, E](request: Request, spanName: String)(
    zio: Carriers => ZIO[R, E, Response]
  )(using
    tracing: Tracing
  ): ZIO[R, E, Response] =
    val carriers = Carriers(
      incomingHeadersCarrier(request.headers),
      OutgoingContextCarrier.default()
    )
    (
      tracing.inject(TraceContextPropagator.default, carriers.output) *>
        zio(carriers) <*
        ZIO.logInfo(s"Headers: ${carriers.outputHeaders}")
    ) @@ tracing.aspects.extractSpan(TraceContextPropagator.default, carriers.input, spanName, SpanKind.SERVER) @@
      PrometheusMetrics.requestHandlerTimer.tagged("endpoint", spanName).trackDuration

  end withTracingCarriers

  def incomingHeadersCarrier(initial: Headers): IncomingContextCarrier[Headers] =
    new IncomingContextCarrier[Headers]:
      override val kernel: Headers = Headers(initial)
      override def getAllKeys(carrier: Headers): Iterable[String] =
        carrier.headers.headers.map(_.headerName)
      override def getByKey(carrier: Headers, key: String): Option[String] =
        carrier.headers.get(key)
    end new
  end incomingHeadersCarrier

  final case class TracingBaggage(tracing: Tracing, baggage: Baggage)
  final case class Carriers(
    input: IncomingContextCarrier[Headers],
    output: OutgoingContextCarrier[mutable.Map[String, String]]
  ):
    def outputHeaders: Map[String, String] = output.kernel.toMap
  end Carriers

end AppTracing
