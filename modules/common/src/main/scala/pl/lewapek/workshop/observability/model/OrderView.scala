package pl.lewapek.workshop.observability.model

import pl.lewapek.workshop.observability.config.VariantConfig
import zio.json.{JsonDecoder, JsonEncoder}

import java.time.Instant

final case class OrderView(
  id: OrderId,
  variant: VariantConfig,
  products: List[WithVariant[ProductInfo]],
  remarks: Option[String],
  date: Instant
) derives JsonEncoder,
    JsonDecoder
