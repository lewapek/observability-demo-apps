package pl.lewapek.workshop.observability.http

import pl.lewapek.workshop.observability.config.VariantConfig
import pl.lewapek.workshop.observability.metrics.TracingService
import pl.lewapek.workshop.observability.model.{OrderId, OrderInput}
import pl.lewapek.workshop.observability.service.OrderService
import zio.*
import zio.http.*
import zio.http.Method.{GET, POST}
import zio.json.*

object AppRoutes:
  def make(tracingService: TracingService, variantConfig: VariantConfig) =
    given VariantConfig = variantConfig
    import tracingService.*
    Http
      .collectZIO[Request] {
        case request @ POST -> !! / "app" / "order" =>
          withTracing(request, "add-order") {
            for
              input <- request.body.jsonAs[OrderInput]
              added <- OrderService.add(input)
            yield added.jsonVariantResponse
          }
        case request @ GET -> !! / "app" / "order" / long(id) =>
          withTracing(request, "/app/order/id") {
            OrderService.get(OrderId(id)).map(_.jsonVariantResponse)
          }
        case request @ GET -> !! / "app" / "order" =>
          withTracing(request, "/app/order") {
            (for
              idsQueryParam <- ZIO.fromOption(request.url.queryParams.get("ids"))
              _             <- tracing.setAttribute("ids", idsQueryParam.mkString("[", ",", "]"))
              ids = idsQueryParam.flatMap(_.split(',')).flatMap(_.toLongOption).map(OrderId(_))
              result <- OrderService.get(ids).asSomeError
            yield result).unsome
              .someOrElseZIO(OrderService.all)
              .map(_.jsonVariantResponse)
          }
      }
      .tapErrorZIO(appError => ZIO.logWarning(s"Error processing request: ${appError.show}"))
      .mapError(appError => Response.status(Status.InternalServerError))
  end make

end AppRoutes
