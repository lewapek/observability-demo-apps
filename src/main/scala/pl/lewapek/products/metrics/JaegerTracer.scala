package pl.lewapek.products.metrics

import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.{SdkTracerProvider, SpanLimits}
import io.opentelemetry.sdk.trace.`export`.SimpleSpanProcessor
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes
import pl.lewapek.products.config.TracingConfig
import zio.{RLayer, Task, ZIO, ZLayer}

object JaegerTracer:
  def live: RLayer[TracingConfig, Tracer] = ZLayer(
    for
      config <- ZIO.service[TracingConfig]
      tracer <- makeTracer(config)
    yield tracer
  )

  private def makeTracer(config: TracingConfig): Task[Tracer] =
    for
      spanExporter  <- ZIO.attempt(OtlpGrpcSpanExporter.builder().setEndpoint(config.host).build())
      spanProcessor <- ZIO.succeed(SimpleSpanProcessor.create(spanExporter))
      tracerProvider <-
        ZIO.attempt(
          SdkTracerProvider
            .builder()
            .setResource(Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "workshop-products")))
            .addSpanProcessor(spanProcessor)
            .build()
        )
      openTelemetry <- ZIO.succeed(OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build())
      tracer        <- ZIO.succeed(openTelemetry.getTracer("workshop"))
    yield tracer

end JaegerTracer
