package pl.lewapek.workshop.observability.service

import cats.syntax.foldable.*
import cats.syntax.show.*
import pl.lewapek.workshop.observability.AppError
import pl.lewapek.workshop.observability.Bootstrap.SttpBackendType
import pl.lewapek.workshop.observability.config.ProductsServiceClientConfig
import pl.lewapek.workshop.observability.http.SttpUtils
import pl.lewapek.workshop.observability.metrics.TracingService.TracingHeaders
import pl.lewapek.workshop.observability.metrics.PrometheusMetrics
import pl.lewapek.workshop.observability.metrics.PrometheusMetrics.GetVariant
import pl.lewapek.workshop.observability.model.*
import sttp.client3.{asStringAlways, basicRequest}
import zio.*
import zio.json.*
import zio.json.ast.Json
import zio.telemetry.opentelemetry.tracing.Tracing

class ProductServiceClient(backend: SttpBackendType, productsConfig: ProductsServiceClientConfig, tracing: Tracing)
  extends SttpUtils(
    backend,
    basicRequest.response(asStringAlways.map(_.fromJson[Json])).contentType("application/json")
  ) with CommonClient(productsConfig.uri):
  import tracing.aspects.*

  def addProduct(input: ProductInfoInput)(using TracingHeaders): IO[AppError, WithVariant[ProductInfo]] =
    sendTraceJson(
      _.post(productsConfig.uri.addPath("app", "product")).body(input.toJson)
    ) @@ span("post:/app/product")

  def product(id: ProductId)(using TracingHeaders): IO[AppError, WithVariant[Option[ProductInfo]]] =
    sendTraceJson(
      _.get(productsConfig.uri.addPath("app", "product", id.show))
    ) @@ span("get:/app/product/id") @@ PrometheusMetrics.workshopProductRequests
      .tagged(GetVariant.labelSingle)
      .fromConst(1)

  def products(ids: List[ProductId])(using TracingHeaders): IO[AppError, WithVariant[List[ProductInfo]]] =
    sendTraceJson(
      _.get(productsConfig.uri.addPath("app", "product").addParam("ids", ids.map(_.show).intercalate(",")))
    ) @@ span("get:/app/product?ids") @@ PrometheusMetrics.workshopProductRequests
      .tagged(GetVariant.labelBatch)
      .fromConst(1)

  def products(using TracingHeaders): IO[AppError, WithVariant[List[ProductInfo]]] =
    sendTraceJson(
      _.get(productsConfig.uri.addPath("app", "product"))
    ) @@ span("get:/app/product") @@ PrometheusMetrics.workshopProductRequests
      .tagged(GetVariant.labelAll)
      .fromConst(1)

end ProductServiceClient

object ProductServiceClient:
  val layer = ZLayer.fromFunction(ProductServiceClient(_, _, _))
end ProductServiceClient
