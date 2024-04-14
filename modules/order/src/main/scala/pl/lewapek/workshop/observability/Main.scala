package pl.lewapek.workshop.observability

import doobie.util.transactor.Transactor
import io.opentelemetry.api.trace.Tracer
import pl.lewapek.workshop.observability.config.{CommonConfig, ProductsServiceClientConfig, VariantConfig}
import pl.lewapek.workshop.observability.db.PostgresDatabase
import pl.lewapek.workshop.observability.http.{AppRoutes, HttpServer}
import pl.lewapek.workshop.observability.metrics.{OtelTracer, TracingService}
import pl.lewapek.workshop.observability.service.{
  ForwardingService,
  Healthcheck,
  ManualHealthStateService,
  OrderService
}
import zio.*
import zio.metrics.connectors.prometheus
import zio.metrics.jvm.DefaultJvmMetrics
import zio.telemetry.opentelemetry.baggage.Baggage
import zio.telemetry.opentelemetry.context.ContextStorage
import zio.telemetry.opentelemetry.tracing.Tracing

object Main extends ZIOAppDefault:
  type Requirements = Bootstrap.CommonRequirements & OrderService & Transactor[Task]

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Bootstrap.logger >>> Runtime.setConfigProvider(CommonConfig.provider)

  private val program =
    for
      _              <- ZIO.logInfo("Starting Products management")
      tracingService <- ZIO.service[TracingService]
      variantConfig  <- ZIO.service[VariantConfig]
      _ <- HttpServer.run(
        AppRoutes.make(tracingService, variantConfig),
        Healthcheck.manualToggle ++ Healthcheck.postgres
      )
    yield ()

  private val layer = ZLayer.make[Requirements](
    CommonConfig.layer,
    prometheus.publisherLayer,
    prometheus.prometheusLayer,
    DefaultJvmMetrics.live.unit,
    Bootstrap.sttpBackendLayer,
    ForwardingService.layer,
    OrderService.layer,
    PostgresDatabase.transactorLive,
    ManualHealthStateService.layer,
    Tracing.live,
    Baggage.live(),
    TracingService.layer,
    ContextStorage.fiberRef,
    OtelTracer.live
  )

  override val run: ZIO[Any, Throwable, Any] =
    program.provide(layer)
end Main
