package pl.lewapek.workshop.observability

import io.opentelemetry.api.trace.Tracer
import pl.lewapek.workshop.observability.config.{CommonConfig, ProductsServiceClientConfig, VariantConfig}
import pl.lewapek.workshop.observability.db.PostgresDatabase
import pl.lewapek.workshop.observability.http.{AppRoutes, HttpServer}
import pl.lewapek.workshop.observability.metrics.{JaegerTracer, TracingService}
import pl.lewapek.workshop.observability.service.{ForwardingService, OrderService}
import zio.*
import zio.metrics.connectors.prometheus
import zio.telemetry.opentelemetry.baggage.Baggage
import zio.telemetry.opentelemetry.context.ContextStorage
import zio.telemetry.opentelemetry.tracing.Tracing

object Main extends ZIOAppDefault:
  type Requirements = Bootstrap.CommonRequirements & OrderService

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Bootstrap.logger >>> Runtime.setConfigProvider(CommonConfig.provider)

  private val program =
    for
      _              <- ZIO.logInfo("Starting Products management")
      tracingService <- ZIO.service[TracingService]
      variantConfig  <- ZIO.service[VariantConfig]
      _              <- HttpServer.run(AppRoutes.make(tracingService, variantConfig))
    yield ()

  private val layer = ZLayer.make[Requirements](
    CommonConfig.layer,
    prometheus.publisherLayer,
    prometheus.prometheusLayer,
    Bootstrap.sttpBackendLayer,
    ForwardingService.layer,
    OrderService.layer,
    PostgresDatabase.transactorLive,
    Tracing.live,
    Baggage.live(),
    TracingService.layer,
    ContextStorage.fiberRef,
    JaegerTracer.live
  )

  override val run: ZIO[Any, Throwable, Any] =
    program.provide(layer)
end Main
