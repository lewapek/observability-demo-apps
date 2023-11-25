package pl.lewapek.workshop.observability.service

import pl.lewapek.workshop.observability.AppError
import pl.lewapek.workshop.observability.Bootstrap.SttpBackendType
import pl.lewapek.workshop.observability.config.VariantConfig
import pl.lewapek.workshop.observability.metrics.TracingService.TracingHeaders
import pl.lewapek.workshop.observability.model.WithVariant.*
import pl.lewapek.workshop.observability.model.{Order, OrderView, ProductInfo, WithVariant}
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

  private val productsForOrder: TracingHeaders => Order => IO[AppError, List[WithVariant[ProductInfo]]] =
    if variantConfig.version == 3 then
      (headers: TracingHeaders) =>
        (order: Order) =>
          productServiceClient
            .products(order.products)(using headers)
            .map(result => result.value.map(_.withVariant(result.variant)))
    else if variantConfig.version == 2 then
      (headers: TracingHeaders) =>
        (order: Order) =>
          ZIO
            .foreachPar(order.products)(productServiceClient.product(_)(using headers))
            .withParallelism(5)
            .map(_.collect { case WithVariant(config, Some(product)) => WithVariant(config, product) })
    else
      (headers: TracingHeaders) =>
        (order: Order) =>
          ZIO
            .foreach(order.products)(productServiceClient.product(_)(using headers))
            .map(_.collect { case WithVariant(config, Some(product)) => WithVariant(config, product) })
  end productsForOrder

  def allOrders(offset: Offset, limit: Limit)(using TracingHeaders): IO[AppError, List[OrderView]] =
    for
      orderResult <- orderServiceClient.orders
      orders = orderResult.value
      views <- ZIO
        .foreachPar(orders)(order =>
          productsForOrder(summon[TracingHeaders])(order).map(products =>
            OrderView(order.id, orderResult.variant, products, order.remarks, order.date)
          )
        )
        .withParallelism(4)
    yield views

end ViewService

object ViewService:
  def allOrders(offset: Offset, limit: Limit)(using TracingHeaders): ZIO[ViewService, AppError, List[OrderView]] =
    ZIO.serviceWithZIO[ViewService](_.allOrders(offset, limit))

  val layer = ZLayer.fromFunction(ViewService(_, _, _, _, _))
end ViewService
