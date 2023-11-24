package pl.lewapek.workshop.observability.service

import cats.syntax.foldable.*
import cats.syntax.show.*
import pl.lewapek.workshop.observability.AppError
import pl.lewapek.workshop.observability.Bootstrap.SttpBackendType
import pl.lewapek.workshop.observability.config.OrdersServiceClientConfig
import pl.lewapek.workshop.observability.http.SttpUtils
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

  def addOrder(headers: Map[String, String], input: OrderInput): IO[AppError, Order] =
    send(
      headers,
      _.post(orderConfig.uri.addPath("app", "order")).body(input.toJson)
    ) @@ span("post:/app/order")

  def order(headers: Map[String, String], id: OrderId): IO[AppError, Option[Order]] =
    send(
      headers,
      _.get(orderConfig.uri.addPath("app", "order", id.show))
    ) @@ span("get:/app/order/id")

  def orders(headers: Map[String, String], ids: List[OrderId]): IO[AppError, List[Order]] =
    send(
      headers,
      _.get(orderConfig.uri.addPath("app", "order").addParam("ids", ids.map(_.show).intercalate(",")))
    ) @@ span("get:/app/order?ids")

  def orders(headers: Map[String, String]): IO[AppError, List[Order]] =
    send(
      headers,
      _.get(orderConfig.uri.addPath("app", "order"))
    ) @@ span("get:/app/product")

end OrderServiceClient

object OrderServiceClient:
  val layer = ZLayer.fromFunction(OrderServiceClient(_, _, _))
end OrderServiceClient
