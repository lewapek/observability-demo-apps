package pl.lewapek.workshop.observability.http

import pl.lewapek.workshop.observability.metrics.AppTracing
import pl.lewapek.workshop.observability.metrics.AppTracing.*
import pl.lewapek.workshop.observability.model.{OrderId, OrderInput}
import pl.lewapek.workshop.observability.service.OrderService
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
        case request @ POST -> !! / "app" / "order" =>
          AppTracing.withTracing(request, "add-order") {
            for
              input <- request.body.jsonAs[OrderInput]
              added <- OrderService.add(input)
            yield Response.json(added.toJson)
          }
        case request @ GET -> !! / "app" / "order" / long(id) =>
          AppTracing.withTracing(request, "/app/order/id") {
            OrderService.get(OrderId(id)).map(_.toJson).map(Response.json)
          }
        case request @ GET -> !! / "app" / "order" =>
          AppTracing.withTracing(request, "/app/order") {
            (for
              idsQueryParam <- ZIO.fromOption(request.url.queryParams.get("ids"))
              _             <- tracing.setAttribute("ids", idsQueryParam.mkString("[", ",", "]"))
              ids = idsQueryParam.flatMap(_.split(',')).flatMap(_.toLongOption).map(OrderId(_))
              result <- OrderService.get(ids).asSomeError
            yield result).unsome
              .someOrElseZIO(OrderService.all)
              .map(_.toJson)
              .map(Response.json)
          }
      }
      .tapErrorZIO(appError => ZIO.logWarning(s"Error processing request: ${appError.show}"))
      .mapError(appError => Response.status(Status.InternalServerError))
  end make

end AppRoutes
