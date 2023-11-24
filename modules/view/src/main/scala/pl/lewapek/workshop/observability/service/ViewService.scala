package pl.lewapek.workshop.observability.service

import pl.lewapek.workshop.observability.AppError
import pl.lewapek.workshop.observability.Bootstrap.SttpBackendType
import pl.lewapek.workshop.observability.config.VariantConfig
import pl.lewapek.workshop.observability.model.{Order, OrderView, ProductInfo}
import pl.lewapek.workshop.observability.types.{Limit, Offset}
import zio.*
import zio.telemetry.opentelemetry.tracing.Tracing

class ViewService(
  variantConfig: VariantConfig,
  sttpBackend: SttpBackendType,
  productServiceClient: ProductServiceClient,
  orderServiceClient: OrderServiceClient,
  tracing: Tracing
):

  private val productsForOrder: Map[String, String] => Order => IO[AppError, List[ProductInfo]] =
    if variantConfig.version == 3 then
      (headers: Map[String, String]) => (order: Order) => productServiceClient.products(headers, order.products)
    else if variantConfig.version == 2 then
      (headers: Map[String, String]) =>
        (order: Order) =>
          ZIO.foreachPar(order.products)(productServiceClient.product(headers, _)).withParallelism(5).map(_.flatten)
    else
      (headers: Map[String, String]) =>
        (order: Order) => ZIO.foreach(order.products)(productServiceClient.product(headers, _)).map(_.flatten)

  def allOrders(headers: Map[String, String])(offset: Offset, limit: Limit): IO[AppError, List[OrderView]] =
    for
      orders <- orderServiceClient.orders(headers)
      views <- ZIO
        .foreachPar(orders)(order =>
          productsForOrder(headers)(order).map(products => OrderView(order.id, products, None, order.date))
        )
        .withParallelism(4)
    yield views

end ViewService

object ViewService:
  def allOrders(
    headers: Map[String, String]
  )(offset: Offset, limit: Limit): ZIO[ViewService, AppError, List[OrderView]] =
    ZIO.serviceWithZIO[ViewService](_.allOrders(headers)(offset, limit))

  val layer = ZLayer.fromFunction(ViewService(_, _, _, _, _))
end ViewService
