package pl.lewapek.workshop.observability.http

import pl.lewapek.workshop.observability.metrics.AppTracing
import pl.lewapek.workshop.observability.metrics.AppTracing.*
import pl.lewapek.workshop.observability.model.*
import pl.lewapek.workshop.observability.service.{ForwardingService, InitLoadService, ViewService}
import pl.lewapek.workshop.observability.types.{Limit, Offset}
import zio.*
import zio.http.*
import zio.http.Method.{GET, POST}
import zio.json.*
import zio.telemetry.opentelemetry.baggage.Baggage
import zio.telemetry.opentelemetry.tracing.Tracing

object AppRoutes:
  def make(tracing: Tracing, baggage: Baggage) =
    given Tracing = tracing
    given Baggage = baggage
    Http
      .collectZIO[Request] {
//        case request @ GET -> !! / "app" / "order" / long(id) =>
//          AppTracing.withTracing(request, "/app/order/id") {
//            ViewService.get(OrderId(id)).map(_.toJson).map(Response.json)
//          }
        case request @ POST -> !! / "app" / "init-load" =>
          AppTracing.withTracingCarriers(request, "/app/init-load") { carriers =>
            InitLoadService.initLoad(carriers.outputHeaders).as(Response.ok)
          }
        case request @ GET -> !! / "app" / "order" =>
          AppTracing.withTracingCarriers(request, "/app/order") { carriers =>
            val offset =
              request.url.queryParams.get("offset").flatMap(_.lastOption.flatMap(Offset.from)).getOrElse(Offset(0))
            val limit =
              request.url.queryParams.get("limit").flatMap(_.lastOption.flatMap(Limit.from)).getOrElse(Limit(20))
            ViewService.allOrders(carriers.outputHeaders)(offset, limit).map(_.toJson).map(Response.json)
          }
      }
      .tapErrorZIO(appError => ZIO.logWarning(s"Error processing request: ${appError.show}"))
      .mapError(appError => Response.status(Status.InternalServerError))
  end make

end AppRoutes
