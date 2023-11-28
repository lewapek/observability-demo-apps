package pl.lewapek.workshop.observability.config

import sttp.model.Uri
import zio.*
import zio.config.*
import zio.config.magnolia.DeriveConfig
import zio.config.typesafe.TypesafeConfigProvider
import zio.json.{JsonDecoder, JsonEncoder}
import zio.metrics.connectors.MetricsConfig

object CommonConfig:
  type Requirements = HttpConfig & VariantConfig & DbConfig & ForwardingServiceConfig & MetricsConfig & TracingConfig &
    ProductsServiceClientConfig & OrdersServiceClientConfig

  val provider: ConfigProvider =
    TypesafeConfigProvider.fromResourcePath(enableCommaSeparatedValueAsList = true).kebabCase

  private val http              = DeriveConfig.derived[HttpConfig].desc.nested("http")
  private val db                = DeriveConfig.derived[DbConfig].desc.nested("db")
  private val variant           = DeriveConfig.derived[VariantConfig].desc.nested("variant")
  private val forwardingService = DeriveConfig.derived[ForwardingServiceConfig].desc.nested("forwarding-service")
  private val productServiceClient =
    DeriveConfig.derived[ProductsServiceClientConfig].desc.nested("product-service-client")
  private val orderServiceClient =
    DeriveConfig.derived[OrdersServiceClientConfig].desc.nested("order-service-client")
  private val metrics = Config.duration("interval").nested("metrics").map(MetricsConfig(_))
  private val tracing = DeriveConfig.derived[TracingConfig].desc.nested("tracing")

  private[config] def mkLayer[T: Tag](config: Config[T]) = ZLayer(ZIO.config(config))

  val layer: ULayer[Requirements] =
    (
      mkLayer(http) ++
        mkLayer(variant) ++
        mkLayer(db) ++
        mkLayer(forwardingService) ++
        mkLayer(metrics) ++
        mkLayer(tracing) ++
        mkLayer(productServiceClient) ++
        mkLayer(orderServiceClient)
    )
      .tapError(e => ZIO.logError(s"Error reading config: ${e.getMessage}"))
      .orDie

  given DeriveConfig[Uri] =
    DeriveConfig[String].mapOrFail(string => Uri.parse(string).left.map(Config.Error.InvalidData(Chunk.empty, _)))
end CommonConfig

final case class HttpConfig(port: Int)
final case class ForwardingServiceConfig(uri: Uri, maxTtl: Int)
final case class TracingConfig(host: String, tracerName: String)
final case class VariantConfig(version: Int, namespace: String) derives JsonEncoder, JsonDecoder

final case class DbConfig(
  host: String,
  port: Int,
  user: String,
  password: String,
  databaseName: String,
  driver: String,
  connectionTimeout: Int,
  minimumIdle: Int,
  maximumPoolSize: Int
):
  def jdbcUrl: String = s"jdbc:postgresql://$host:$port/$databaseName"
end DbConfig
final case class ProductsServiceClientConfig(uri: Uri)
final case class OrdersServiceClientConfig(uri: Uri)
