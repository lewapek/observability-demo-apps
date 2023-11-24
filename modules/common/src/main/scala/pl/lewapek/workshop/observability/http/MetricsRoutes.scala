package pl.lewapek.workshop.observability.http

import zio.ZIO
import zio.http.Method.GET
import zio.http.*
import zio.metrics.connectors.prometheus.PrometheusPublisher

object MetricsRoutes:
  val make =
    for publisher <- ZIO.service[PrometheusPublisher]
    yield Http.collectZIO[Request] { case GET -> !! / "metrics" =>
      publisher.get.map(Response.text)
    }
end MetricsRoutes
