package pl.lewapek.workshop.observability.http

import io.opentelemetry.api.trace.SpanKind
import pl.lewapek.workshop.observability.metrics.{AppTracing, PrometheusMetrics}
import pl.lewapek.workshop.observability.model.{ProductId, ProductInfo, ProductInfoInput}
import pl.lewapek.workshop.observability.service.ForwardingService.ForwardRequestInput
import pl.lewapek.workshop.observability.service.{ForwardingService, ProductService}
import zio.*
import zio.http.*
import zio.http.Method.{GET, POST}
import zio.json.*
import zio.telemetry.opentelemetry.baggage.Baggage
import zio.telemetry.opentelemetry.baggage.propagation.BaggagePropagator
import zio.telemetry.opentelemetry.context.OutgoingContextCarrier
import zio.telemetry.opentelemetry.tracing.Tracing
import zio.telemetry.opentelemetry.tracing.propagation.TraceContextPropagator

object AppRoutes:
  def make(tracing: Tracing, baggage: Baggage) =
    given Tracing = tracing
    given Baggage = baggage
    import tracing.aspects.*

    Http
      .collectZIO[Request] {
        case request @ POST -> !! / "app" / "product" =>
          AppTracing.withTracing(request, "add-product") {
            for
              input <- request.body.jsonAs[ProductInfoInput]
              added <- ProductService.add(input)
            yield Response.json(added.toJson)
          }
        case request @ GET -> !! / "app" / "product" / long(id) =>
          AppTracing.withTracing(request, "/app/product/id") {
            ProductService.get(ProductId(id)).map(_.toJson).map(Response.json)
          }
        case request @ GET -> !! / "app" / "product" =>
          AppTracing.withTracing(request, "/app/product") {
            (for
              idsQueryParam <- ZIO.fromOption(request.url.queryParams.get("ids"))
              _             <- tracing.setAttribute("ids", idsQueryParam.mkString("[", ",", "]"))
              ids = idsQueryParam.flatMap(_.split(',')).flatMap(_.toLongOption).map(ProductId(_))
              result <- ProductService.get(ids).asSomeError
            yield result).unsome
              .someOrElseZIO(ProductService.all)
              .map(_.toJson)
              .map(Response.json)
          }
      }
      .tapErrorZIO(appError => ZIO.logWarning(s"Error processing request: ${appError.show}"))
      .mapError(appError => Response.status(Status.InternalServerError))
  end make

end AppRoutes
