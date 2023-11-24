package pl.lewapek.workshop.observability

import io.opentelemetry.api.trace.StatusCode
import pl.lewapek.workshop.observability.config.{HttpConfig, VariantConfig}
import pl.lewapek.workshop.observability.metrics.JaegerTracer
import pl.lewapek.workshop.observability.service.ForwardingService
import sttp.capabilities
import sttp.capabilities.zio.ZioStreams
import sttp.client3.SttpBackend
import sttp.client3.httpclient.zio.HttpClientZioBackend
import sttp.client3.logging.{LogLevel, Logger, LoggingBackend}
import zio.logging.slf4j.bridge.Slf4jBridge
import zio.logging.{ConsoleLoggerConfig, LogFilter, LogFormat, consoleJsonLogger, removeDefaultLoggers}
import zio.metrics.connectors.MetricsConfig
import zio.metrics.connectors.prometheus.PrometheusPublisher
import zio.telemetry.opentelemetry.baggage.Baggage
import zio.telemetry.opentelemetry.context.ContextStorage
import zio.telemetry.opentelemetry.tracing.{StatusMapper, Tracing}
import zio.{Cause, Task, ZIO, ZLayer}

object Bootstrap:
  type CommonRequirements = MetricsConfig & VariantConfig & HttpConfig & PrometheusPublisher & Tracing & Baggage &
    ContextStorage & ForwardingService
  type SttpBackendType = SttpBackend[Task, ZioStreams & capabilities.WebSockets]

  val statusMapper: StatusMapper[Throwable, Any] = StatusMapper.failureThrowable(_ => StatusCode.UNSET)

  lazy val logger: ZLayer[Any, Nothing, Any] =
    removeDefaultLoggers >>> consoleJsonLogger(
      ConsoleLoggerConfig.default.copy(
        format = LogFormat.default + LogFormat.allAnnotations,
        filter = LogFilter.logLevelByName(zio.LogLevel.Info)
      )
    ) >>> Slf4jBridge.initialize

  lazy val sttpBackendLayer: ZLayer[Any, Throwable, SttpBackendType] =
    ZLayer.scoped(
      HttpClientZioBackend
        .scoped()
        .map(delegate => LoggingBackend(delegate, ZIOSttpLogger, logResponseBody = true, logResponseHeaders = true))
    )

  private object ZIOSttpLogger extends Logger[Task]:
    override def apply(level: LogLevel, message: => String): Task[Unit] = level match
      case LogLevel.Trace => ZIO.logTrace(message)
      case LogLevel.Debug => ZIO.logDebug(message)
      case LogLevel.Info  => ZIO.logInfo(message)
      case LogLevel.Warn  => ZIO.logWarning(message)
      case LogLevel.Error => ZIO.logError(message)
    end apply
    override def apply(level: LogLevel, message: => String, t: Throwable): Task[Unit] = level match
      case LogLevel.Trace => ZIO.logTraceCause(message, Cause.fail(t))
      case LogLevel.Debug => ZIO.logDebugCause(message, Cause.fail(t))
      case LogLevel.Info  => ZIO.logInfoCause(message, Cause.fail(t))
      case LogLevel.Warn  => ZIO.logWarningCause(message, Cause.fail(t))
      case LogLevel.Error => ZIO.logErrorCause(message, Cause.fail(t))
    end apply

  end ZIOSttpLogger

end Bootstrap
