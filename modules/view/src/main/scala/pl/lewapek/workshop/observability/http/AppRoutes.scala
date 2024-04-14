package pl.lewapek.workshop.observability.http

import pl.lewapek.workshop.observability.config.VariantConfig
import pl.lewapek.workshop.observability.metrics.TracingService
import pl.lewapek.workshop.observability.model.*
import pl.lewapek.workshop.observability.service.{InitLoadService, TrafficGenerator, ViewService}
import pl.lewapek.workshop.observability.types.{Limit, Offset}
import zio.*
import zio.http.*
import zio.http.Method.*
import zio.json.*

object AppRoutes:
  def make(tracingService: TracingService, variantConfig: VariantConfig) =
    given VariantConfig = variantConfig
    import tracingService.*
    Http
      .collectZIO[Request] {
        case request @ POST -> !! / "app" / "init-load" =>
          withTracingCarriers(request, "/app/init-load") { carriers =>
            InitLoadService.initLoad(using carriers.outputHeaders).as(Response.ok)
          }
        case request @ GET -> !! / "app" / "order" =>
          withTracingCarriers(request, "/app/order") { carriers =>
            val offset =
              request.url.queryParams.get("offset").flatMap(_.lastOption.flatMap(Offset.from)).getOrElse(Offset(0))
            val limit =
              request.url.queryParams.get("limit").flatMap(_.lastOption.flatMap(Limit.from)).getOrElse(Limit(20))
            ViewService.allOrders(offset, limit)(using carriers.outputHeaders).map(_.jsonVariantResponse)
          }
        case request @ GET -> !! / "generator" =>
          TrafficGenerator.status.map(_.jsonVariantResponse)
        case request @ PUT -> !! / "generator" =>
          TrafficGenerator.start.map(_.jsonVariantResponse)
        case request @ DELETE -> !! / "generator" =>
          TrafficGenerator.stop.map(_.jsonVariantResponse)
      }
      .tapErrorZIO(appError => ZIO.logWarning(s"Error processing request: ${appError.show}"))
      .mapError(appError => Response.status(Status.InternalServerError))
  end make

end AppRoutes
