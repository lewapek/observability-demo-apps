package pl.lewapek.workshop.observability.http

import pl.lewapek.workshop.observability.Bootstrap
import pl.lewapek.workshop.observability.config.HttpConfig
import pl.lewapek.workshop.observability.metrics.{PrometheusMetrics, TracingService}
import pl.lewapek.workshop.observability.service.Healthcheck
import sttp.tapir.json.jsoniter.*
import zio.*
import zio.http.*
import zio.telemetry.opentelemetry.baggage.Baggage
import zio.telemetry.opentelemetry.tracing.Tracing

object HttpServer:
  private val serverLayer = ZLayer.fromZIO(
    for httpPort <- ZIO.serviceWith[HttpConfig](_.port)
    yield Server.Config.default.port(httpPort)
  ) >>> Server.live

  def run[R <: Bootstrap.CommonRequirements](
    appRoutes: Http[R, Any, Request, Response],
    livenessProbe: Healthcheck[R] = Healthcheck.empty
  ) = (
    for
      tracingService <- ZIO.service[TracingService]
      readiness = Healthcheck(
        "Async jobs running check",
        PrometheusMetrics.asyncJobsInProgress.value.map(jobs =>
          if jobs.value < 5 then Healthcheck.Status.Ok else Healthcheck.Status.Error
        )
      )
      healthcheckRoutes <- HealthcheckRoutes.make(livenessProbe, readiness, tracingService)
      metricsRoutes     <- MetricsRoutes.make
      commonRoutes = CommonRoutes.make(tracingService)
      routes       = healthcheckRoutes ++ commonRoutes ++ appRoutes ++ metricsRoutes
      port <- Server.install[Bootstrap.CommonRequirements & R](routes.withDefaultErrorResponse)
      _    <- ZIO.logInfo(s"Http server started on port $port")
      _    <- ZIO.never
    yield ()
  ).provideSome[Bootstrap.CommonRequirements & R](serverLayer)
end HttpServer
