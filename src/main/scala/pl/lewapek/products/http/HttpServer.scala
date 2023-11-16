package pl.lewapek.products.http

import caliban.interop.tapir.HttpInterpreter
import caliban.{CalibanError, ZHttpAdapter}
import pl.lewapek.products.Bootstrap
import pl.lewapek.products.config.HttpConfig
import pl.lewapek.products.graphql.GraphqlApi
import pl.lewapek.products.service.Healthcheck
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

  val run = (
    for
      tracing            <- ZIO.service[Tracing]
      baggage            <- ZIO.service[Baggage]
      graphqlInterpreter <- GraphqlApi.api.interpreter.map(GraphqlApi.handleError[Bootstrap.Requirements])
      healthcheckRoutes  <- HealthcheckRoutes.make(Healthcheck.empty, tracing)
      graphQLRoutes = Http.collectHttp[Request] { case _ -> !! / "graphql" =>
        ZHttpAdapter.makeHttpService[Bootstrap.Requirements, CalibanError](HttpInterpreter(graphqlInterpreter))
      }
      appRoutes      = AppRoutes.make(tracing, baggage)
      metricsRoutes <- MetricsRoutes.make
      routes         = healthcheckRoutes ++ graphQLRoutes ++ appRoutes ++ metricsRoutes
      port          <- Server.install[Bootstrap.Requirements](routes.withDefaultErrorResponse)
      _             <- ZIO.logInfo(s"Http server started on port $port")
      _             <- ZIO.never
    yield ()
  ).provideSome[Bootstrap.Requirements](serverLayer)
end HttpServer
