package pl.lewapek.products.http

import zio.ZIO
import zio.http.*
import zio.http.Method.GET
import zio.metrics.connectors.prometheus.PrometheusPublisher

object RecursiveRoutes:
  val make =
    for publisher <- ZIO.service[PrometheusPublisher]
    yield Http.collectZIO[Request] { case GET -> !! / "metrics" =>
      publisher.get.map(Response.text)
    }
end RecursiveRoutes
