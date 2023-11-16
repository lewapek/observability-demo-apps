package pl.lewapek.products.http

import io.opentelemetry.api.trace.{SpanKind, StatusCode}
import pl.lewapek.products.metrics.PrometheusMetrics
import pl.lewapek.products.service.Healthcheck
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

  def make[R](healthcheck: Healthcheck[R], tracing: Tracing)(using
      Trace
  ): ZIO[Any, Nothing, Http[R, Nothing, Request, Response]] =
    import tracing.aspects.*
    for start <- currentTimeSeconds
    yield Http.collectZIO[Request] { case GET -> !! / "healthcheck" =>
      val zio =
        for
          uptimeSeconds <- currentTimeSeconds.map(_ - start)
          _             <- tracing.setAttribute("a", "b")
          _             <- tracing.addEvent("before healthcheck")
          checks        <- healthcheck.run @@ PrometheusMetrics.countHealthcheck.fromConst(1)
          _             <- tracing.addEvent("after healthcheck")
          globalStatus   = checks.map(_.status).reduceIdentity
        yield Response
          .json(HealthcheckResponse(uptimeSeconds, globalStatus, checks).toJson)
          .withStatus(httpStatusFrom(globalStatus))
      zio @@ root("root span", SpanKind.INTERNAL, statusMapper = statusMapper)
    }
  end make

  private def httpStatusFrom(globalStatus: Healthcheck.Status) =
    if globalStatus.isOk then Status.Ok else Status.InternalServerError

  final case class HealthcheckResponse(
      uptimeSeconds: Long,
      globalStatus: Healthcheck.Status,
      details: Chunk[Healthcheck.NamedStatus]
  )
  object HealthcheckResponse:
    given JsonEncoder[HealthcheckResponse] = DeriveJsonEncoder.gen[HealthcheckResponse]

end HealthcheckRoutes
