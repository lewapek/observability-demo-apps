package pl.lewapek.workshop.observability.service

import cats.syntax.foldable.*
import cats.syntax.show.*
import pl.lewapek.workshop.observability.AppError
import pl.lewapek.workshop.observability.Bootstrap.SttpBackendType
import pl.lewapek.workshop.observability.config.ProductsServiceClientConfig
import pl.lewapek.workshop.observability.http.SttpUtils
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
  ):
  import tracing.aspects.*

  def addProduct(headers: Map[String, String], input: ProductInfoInput): IO[AppError, ProductInfo] =
    send(
      headers,
      _.post(productsConfig.uri.addPath("app", "product")).body(input.toJson)
    ) @@ span("post:/app/product")

  def product(headers: Map[String, String], id: ProductId): IO[AppError, Option[ProductInfo]] =
    send(
      headers,
      _.get(productsConfig.uri.addPath("app", "product", id.show))
    ) @@ span("get:/app/product/id")

  def products(headers: Map[String, String], ids: List[ProductId]): IO[AppError, List[ProductInfo]] =
    send(
      headers,
      _.get(productsConfig.uri.addPath("app", "product").addParam("ids", ids.map(_.show).intercalate(",")))
    ) @@ span("get:/app/product?ids")

  def products(headers: Map[String, String]): IO[AppError, List[ProductInfo]] =
    send(
      headers,
      _.get(productsConfig.uri.addPath("app", "product"))
    ) @@ span("get:/app/product")

end ProductServiceClient

object ProductServiceClient:
  val layer = ZLayer.fromFunction(ProductServiceClient(_, _, _))
end ProductServiceClient
