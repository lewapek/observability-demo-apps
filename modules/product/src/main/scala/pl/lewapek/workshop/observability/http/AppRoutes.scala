package pl.lewapek.workshop.observability.http

import pl.lewapek.workshop.observability.config.VariantConfig
import pl.lewapek.workshop.observability.metrics.TracingService
import pl.lewapek.workshop.observability.model.{ProductId, ProductInfo, ProductInfoInput}
import pl.lewapek.workshop.observability.service.ProductService
import zio.*
import zio.http.*
import zio.http.Method.{GET, PATCH, POST}
import zio.json.*

object AppRoutes:
  def make(tracingService: TracingService, variantConfig: VariantConfig) =
    given VariantConfig = variantConfig
    import tracingService.*
    Http
      .collectZIO[Request] {
        case request @ POST -> !! / "app" / "product" =>
          withTracing(request, "add-product") {
            for
              input <- request.body.jsonAs[ProductInfoInput]
              added <- ProductService.add(input)
            yield added.jsonVariantResponse
          }
        case request @ PATCH -> !! / "app" / "product-fun-facts" =>
          withTracing(request, "patch-product") {
            for
              input <- request.body.jsonAs[ProductInfoInput]
              added <- ProductService.updateFunFacts(input)
            yield Response.ok
          }
        case request @ GET -> !! / "app" / "product" / long(id) =>
          withTracing(request, "/app/product/id") {
            ProductService.get(ProductId(id)).map(_.jsonVariantResponse)
          }
        case request @ GET -> !! / "app" / "product" =>
          withTracing(request, "/app/product") {
            (for
              idsQueryParam <- ZIO.fromOption(request.url.queryParams.get("ids"))
              _             <- tracing.setAttribute("ids", idsQueryParam.mkString("[", ",", "]"))
              ids = idsQueryParam.flatMap(_.split(',')).flatMap(_.toLongOption).map(ProductId(_))
              result <- ProductService.get(ids).asSomeError
            yield result).unsome
              .someOrElseZIO(ProductService.all)
              .map(_.jsonVariantResponse)
          }
      }
      .tapErrorZIO(appError => ZIO.logWarning(s"Error processing request: ${appError.show}"))
      .mapError(appError => Response.status(Status.InternalServerError))
  end make

end AppRoutes
