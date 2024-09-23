package pl.lewapek.workshop.observability.service

import pl.lewapek.workshop.observability.Bootstrap.SttpBackendType
import pl.lewapek.workshop.observability.utils.ZIOUtils.*
import ForwardingService.{ForwardRequestInput, ForwardingResponse}
import pl.lewapek.workshop.observability.AppError
import pl.lewapek.workshop.observability.config.ForwardingServiceConfig
import sttp.client3.{asStringAlways, basicRequest}
import zio.*
import zio.json.*
import zio.json.ast.Json
import zio.telemetry.opentelemetry.tracing.Tracing

class ForwardingService(backend: SttpBackendType, config: ForwardingServiceConfig, tracing: Tracing):
  import tracing.aspects.*

  private val request = basicRequest.response(asStringAlways.map(_.fromJson[Json])).contentType("application/json")

  def forward(input: ForwardRequestInput): IO[AppError, ForwardingResponse] =
    for
      _ <- input.beforeMillisMax1Min.whenDefinedDiscard(millis => ZIO.sleep(millis.millis) @@ span("sleep before"))
      response <- input
        .ensureMaxTtl(config.maxTtl)
        .decrementTtl
        .fold(
          ZIO.logInfo("Forwarding request reached the end").as(ForwardingResponse(0))
        )(decreasedTllRequest =>
          ZIO.logInfo(s"Forwarding request is being sent with decreased ttl: $decreasedTllRequest") *>
            send(input.headers, decreasedTllRequest.toJson)
        ) @@ span("send")
      _ <- input.afterMillisMax1Min.whenDefinedDiscard(millis => ZIO.sleep(millis.millis) @@ span("sleep after"))
    yield response
  end forward

  private def send(headers: Map[String, String], body: String): IO[AppError, ForwardingResponse] =
    backend
      .send(request.headers(headers).body(body).post(config.uri))
      .mapBoth(
        AppError.internal("Error sending forwarding request", _),
        _.body.left
          .map(msg => AppError.internal(s"Couldn't read forwarding response: $msg"))
          .flatMap(
            _.as[ForwardingResponse].left
              .map(msg => AppError.internal(s"Couldn't convert json to response model: $msg"))
          )
      )
      .absolve
  end send

end ForwardingService

object ForwardingService:

  def forward(input: ForwardRequestInput): ZIO[ForwardingService, AppError, ForwardingResponse] =
    ZIO.serviceWithZIO[ForwardingService](_.forward(input))
  final case class ForwardRequestInput(
    ttl: Int,
    maybeHeaders: Option[Map[String, String]], // option, so json decoder can properly treat the absence
    beforeMillis: Option[Int],
    afterMillis: Option[Int]
  ) derives JsonEncoder,
      JsonDecoder:
    def beforeMillisMax1Min: Option[Int]                                  = beforeMillis.map(_.min(1000))
    def afterMillisMax1Min: Option[Int]                                   = afterMillis.map(_.min(1000))
    def ensureMaxTtl(maxValue: Int): ForwardRequestInput               = copy(ttl = ttl.min(maxValue))
    def decrementTtl: Option[ForwardRequestInput]                      = if ttl <= 0 then None else Some(copy(ttl - 1))
    def withHeaders(headers: Map[String, String]): ForwardRequestInput = copy(maybeHeaders = Some(headers))
    def headers: Map[String, String]                                   = maybeHeaders.getOrElse(Map.empty)
  end ForwardRequestInput

  final case class ForwardingResponse(ttl: Int) derives JsonEncoder, JsonDecoder

  val layer = ZLayer.fromFunction(ForwardingService(_, _, _))
end ForwardingService
