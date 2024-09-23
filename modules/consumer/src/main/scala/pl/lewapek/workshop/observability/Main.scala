package pl.lewapek.workshop.observability

import io.opentelemetry.api.trace.Tracer
import pl.lewapek.workshop.observability.config.{CommonConfig, KafkaConfig, ProductsServiceClientConfig, VariantConfig}
import pl.lewapek.workshop.observability.http.HttpServer
import pl.lewapek.workshop.observability.metrics.{OtelTracer, TracingService}
import pl.lewapek.workshop.observability.service.*
import zio.*
import zio.http.Http
import zio.kafka.consumer.{Consumer, ConsumerSettings}
import zio.metrics.connectors.prometheus
import zio.metrics.jvm.DefaultJvmMetrics
import zio.telemetry.opentelemetry.baggage.Baggage
import zio.telemetry.opentelemetry.context.ContextStorage
import zio.telemetry.opentelemetry.tracing.Tracing

object Main extends ZIOAppDefault:
  type Requirements = Bootstrap.CommonRequirements & KafkaConfig & ProductsConsumer & PrintConsumer

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Bootstrap.logger >>> Runtime.setConfigProvider(CommonConfig.provider)

  private val consumerLayer: ZLayer[KafkaConfig, Throwable, Consumer] =
    ZLayer
      .scoped(
        for
          kafkaConfig <- ZIO.service[KafkaConfig]
          consumer <- Consumer.make(
            ConsumerSettings(kafkaConfig.bootstrapServers)
              .withGroupId(kafkaConfig.group)
//              .withProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.bootstrapServers)
//              .withProperty(AdminClientConfig.CLIENT_ID_CONFIG, "KafkaAdminClient")
          )
        yield consumer
      )
      .tapError(e => ZIO.logError(e.getMessage))

  private val program =
    for
      _ <- ZIO.logInfo("Starting consumer app")
      _ <- HttpServer
        .run(Http.empty, Healthcheck.manualToggle)
        .raceFirst(ProductsConsumer.consume.runDrain)
        .raceFirst(PrintConsumer.consume.runDrain)
    yield ()

  private val layer = ZLayer.make[Requirements](
    CommonConfig.layer,
    prometheus.publisherLayer,
    prometheus.prometheusLayer,
    DefaultJvmMetrics.live.unit,
    Bootstrap.sttpBackendLayer,
    ForwardingService.layer,
    ProductServiceClient.layer,
    ManualHealthStateService.layer,
    Tracing.live,
    Baggage.live(),
    TracingService.layer,
    ContextStorage.fiberRef,
    OtelTracer.live,
    consumerLayer,
    ProductsConsumer.layer,
    PrintConsumer.layer
  )

  override val run: ZIO[Any, Throwable, Any] =
    program.provide(layer)
end Main
