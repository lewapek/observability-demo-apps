package pl.lewapek.workshop.observability.http

import io.opentelemetry.api.trace.{SpanKind, StatusCode}
import pl.lewapek.workshop.observability.metrics.{AppTracing, PrometheusMetrics}
import pl.lewapek.workshop.observability.service.Healthcheck
import zio.http.*
import zio.http.Method.GET
import zio.json.{DeriveJsonEncoder, EncoderOps, JsonEncoder}
import zio.prelude.ForEachOps
import zio.telemetry.opentelemetry.tracing.{StatusMapper, Tracing}
import zio.{Chunk, Clock, Trace, ZIO}

import java.util.concurrent.TimeUnit

object HealthcheckRoutes:
  private val currentTimeSeconds = Clock.currentTime(TimeUnit.SECONDS)

  val statusMapper = StatusMapper.failureThrowable(_ => StatusCode.UNSET)

  def make[R1, R2](liveness: Healthcheck[R1], readiness: Healthcheck[R2], tracing: Tracing)(using
    Trace
  ): ZIO[Any, Nothing, Http[R1 & R2, Nothing, Request, Response]] =
    given Tracing = tracing

    for start <- currentTimeSeconds
    yield Http.collectZIO[Request] {
      case request @ GET -> !! / "liveness" =>
        AppTracing.withTracing(request, "/liveness") {
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
        AppTracing.withTracing(request, "/readiness") {
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

  final case class HealthcheckResponse(
    uptimeSeconds: Long,
    globalStatus: Healthcheck.Status,
    details: Chunk[Healthcheck.NamedStatus]
  ) derives JsonEncoder

end HealthcheckRoutes
