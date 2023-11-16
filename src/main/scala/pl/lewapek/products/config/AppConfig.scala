package pl.lewapek.products.config

import sttp.model.Uri
import zio.*
import zio.config.*
import zio.config.magnolia.DeriveConfig
import zio.config.typesafe.TypesafeConfigProvider
import zio.metrics.connectors.MetricsConfig

object AppConfig:
  type ConfigRequirements = HttpConfig & ForwardingServiceConfig & MetricsConfig & TracingConfig

  val provider: ConfigProvider =
    TypesafeConfigProvider.fromResourcePath(enableCommaSeparatedValueAsList = true).kebabCase

  private val http              = DeriveConfig.derived[HttpConfig].desc.nested("http")
  private val forwardingService = DeriveConfig.derived[ForwardingServiceConfig].desc.nested("forwarding-service")
  private val metrics           = Config.duration("interval").nested("metrics").map(MetricsConfig(_))
  private val tracing           = DeriveConfig.derived[TracingConfig].desc.nested("tracing")

  private def layer[T: Tag](config: Config[T]) = ZLayer.fromZIO(ZIO.config(config))

  val all: Layer[Config.Error, ConfigRequirements] =
    layer(http) ++ layer(forwardingService) ++ layer(metrics) ++ layer(tracing)

  private given DeriveConfig[Uri] =
    DeriveConfig[String].mapOrFail(string => Uri.parse(string).left.map(Config.Error.InvalidData(Chunk.empty, _)))
end AppConfig

final case class HttpConfig(port: Int)
final case class ForwardingServiceConfig(uri: Uri)
final case class TracingConfig(host: String)
