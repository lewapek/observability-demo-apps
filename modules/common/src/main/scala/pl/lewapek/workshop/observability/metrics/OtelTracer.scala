package pl.lewapek.workshop.observability.metrics

import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.{SdkTracerProvider, SpanLimits}
import io.opentelemetry.sdk.trace.`export`.SimpleSpanProcessor
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes
import pl.lewapek.workshop.observability.config.TracingConfig
import zio.{RLayer, Task, ZIO, ZLayer}

object OtelTracer:
  def live: RLayer[TracingConfig, Tracer] = ZLayer(
    for
      config <- ZIO.service[TracingConfig]
      tracer <- makeTracer(config)
    yield tracer
  )

  private def makeTracer(config: TracingConfig): Task[Tracer] =
    for
      openTelemetry <-
        if !config.enabled then ZIO.succeed(OpenTelemetry.noop())
        else
          for
            _             <- ZIO.logInfo(s"Creating tracer ${config.tracerName} with host: ${config.host}")
            spanExporter  <- ZIO.attempt(OtlpGrpcSpanExporter.builder().setEndpoint(config.host).build())
            spanProcessor <- ZIO.succeed(SimpleSpanProcessor.create(spanExporter))
            tracerProvider <-
              ZIO.attempt(
                SdkTracerProvider
                  .builder()
                  .setResource(Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, config.tracerName)))
                  .addSpanProcessor(spanProcessor)
                  .build()
              )
          yield OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build()
      tracer <- ZIO.succeed(openTelemetry.getTracer(config.tracerName))
    yield tracer

end OtelTracer
