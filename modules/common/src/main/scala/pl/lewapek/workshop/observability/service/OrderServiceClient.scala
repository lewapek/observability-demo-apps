package pl.lewapek.workshop.observability.service

import cats.syntax.foldable.*
import cats.syntax.show.*
import pl.lewapek.workshop.observability.AppError
import pl.lewapek.workshop.observability.Bootstrap.SttpBackendType
import pl.lewapek.workshop.observability.config.OrdersServiceClientConfig
import pl.lewapek.workshop.observability.http.SttpUtils
import pl.lewapek.workshop.observability.metrics.TracingService.TracingHeaders
import pl.lewapek.workshop.observability.model.*
import sttp.client3.{asStringAlways, basicRequest}
import zio.*
import zio.json.*
import zio.json.ast.Json
import zio.telemetry.opentelemetry.tracing.Tracing

class OrderServiceClient(backend: SttpBackendType, orderConfig: OrdersServiceClientConfig, tracing: Tracing)
  extends SttpUtils(
    backend,
    basicRequest.response(asStringAlways.map(_.fromJson[Json])).contentType("application/json")
  ):
  import tracing.aspects.*

  def addOrder(input: OrderInput)(using TracingHeaders): IO[AppError, WithVariant[Order]] =
    send(
      _.post(orderConfig.uri.addPath("app", "order")).body(input.toJson)
    ) @@ span("post:/app/order")

  def order(id: OrderId)(using TracingHeaders): IO[AppError, WithVariant[Option[Order]]] =
    send(
      _.get(orderConfig.uri.addPath("app", "order", id.show))
    ) @@ span("get:/app/order/id")

  def orders(ids: List[OrderId])(using TracingHeaders): IO[AppError, WithVariant[List[Order]]] =
    send(
      _.get(orderConfig.uri.addPath("app", "order").addParam("ids", ids.map(_.show).intercalate(",")))
    ) @@ span("get:/app/order?ids")

  def orders(using headers: TracingHeaders): IO[AppError, WithVariant[List[Order]]] =
    send(
      _.get(orderConfig.uri.addPath("app", "order"))
    ) @@ span("get:/app/product")

end OrderServiceClient

object OrderServiceClient:
  val layer = ZLayer.fromFunction(OrderServiceClient(_, _, _))
end OrderServiceClient
