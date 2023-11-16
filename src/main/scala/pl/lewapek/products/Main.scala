package pl.lewapek.products

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.Tracer
import pl.lewapek.products.config.AppConfig
import pl.lewapek.products.http.HttpServer
import pl.lewapek.products.metrics.JaegerTracer
import pl.lewapek.products.service.ForwardingService
import zio.*
import zio.metrics.connectors.prometheus
import zio.telemetry.opentelemetry.baggage.Baggage
import zio.telemetry.opentelemetry.context.ContextStorage
import zio.telemetry.opentelemetry.tracing.Tracing

object Main extends ZIOAppDefault:
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Bootstrap.logger >>> Runtime.setConfigProvider(AppConfig.provider)

  private val program =
    for
      _ <- ZIO.logInfo("Starting Products management")
      _ <- HttpServer.run
    yield ()

  private val globalTracerLayer: TaskLayer[Tracer] =
    ZLayer.fromZIO(
      ZIO.attempt(GlobalOpenTelemetry.getTracer("Workshop global tracer"))
    )

  private val layer = ZLayer.make[Bootstrap.Requirements](
    AppConfig.all,
    prometheus.publisherLayer,
    prometheus.prometheusLayer,
    Bootstrap.sttpBackendLayer,
    ForwardingService.layer,
    Tracing.live,
    Baggage.live(),
    ContextStorage.fiberRef,
    JaegerTracer.live
//    ContextStorage.openTelemetryContext,
//    globalTracerLayer
  )

  override val run: ZIO[Any with Scope, Throwable, Any] =
    program.provideSome[Scope](layer)
end Main
