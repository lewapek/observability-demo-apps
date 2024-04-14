package pl.lewapek.workshop.observability.http

import io.opentelemetry.api.trace.StatusCode
import pl.lewapek.workshop.observability.metrics.TracingService.*
import pl.lewapek.workshop.observability.metrics.{PrometheusMetrics, TracingService}
import pl.lewapek.workshop.observability.service.{Healthcheck, ManualHealthStateService}
import zio.http.*
import zio.http.Method.*
import zio.json.{DeriveJsonEncoder, EncoderOps, JsonEncoder}
import zio.prelude.ForEachOps
import zio.telemetry.opentelemetry.tracing.StatusMapper
import zio.{Chunk, Clock, Trace, ZIO}

import java.util.concurrent.TimeUnit

object HealthcheckRoutes:
  private val currentTimeSeconds = Clock.currentTime(TimeUnit.SECONDS)

  val statusMapper = StatusMapper.failureThrowable(_ => StatusCode.UNSET)

  def make[R1, R2](liveness: Healthcheck[R1], readiness: Healthcheck[R2], tracingService: TracingService)(using
    Trace
  ): ZIO[Any, Nothing, Http[R1 & R2 & ManualHealthStateService, Nothing, Request, Response]] =
    import tracingService.*
    for start <- currentTimeSeconds
    yield Http.collectZIO[Request] {
      case request @ PUT -> !! / "liveness" =>
        ManualHealthStateService.setHealthy.as(Response.ok)
      case request @ DELETE -> !! / "liveness" =>
        ManualHealthStateService.setUnhealthy.as(Response.ok)
      case request @ GET -> !! / "liveness" =>
        withTracing(request, "/liveness") {
          for
            uptimeSeconds <- currentTimeSeconds.map(_ - start)
            _             <- tracing.setAttribute("uptime", uptimeSeconds.toString)
            _             <- tracing.addEvent("before run")
            checks        <- liveness.run @@ PrometheusMetrics.countLiveness.fromConst(1)
            _             <- tracing.addEvent("after run")
            globalStatus = checks.map(_.status).reduceIdentity
          yield Response
            .json(HealthcheckResponse(uptimeSeconds, globalStatus, checks).toJson)
            .withStatus(httpStatusFrom(globalStatus))
        }
      case request @ GET -> !! / "readiness" =>
        withTracing(request, "/readiness") {
          for
            uptimeSeconds <- currentTimeSeconds.map(_ - start)
            _             <- tracing.setAttribute("uptime", uptimeSeconds.toString)
            _             <- tracing.addEvent("before run")
            checks        <- readiness.run @@ PrometheusMetrics.countReadiness.fromConst(1)
            _             <- tracing.addEvent("after run")
            globalStatus = checks.map(_.status).reduceIdentity
          yield Response
            .json(HealthcheckResponse(uptimeSeconds, globalStatus, checks).toJson)
            .withStatus(httpStatusFrom(globalStatus))
        }
    }
  end make

  private def httpStatusFrom(globalStatus: Healthcheck.Status) =
    if globalStatus.isOk then Status.Ok else Status.InternalServerError

  private final case class HealthcheckResponse(
    uptimeSeconds: Long,
    globalStatus: Healthcheck.Status,
    details: Chunk[Healthcheck.NamedStatus]
  ) derives JsonEncoder

end HealthcheckRoutes
