package pl.lewapek.workshop.observability.model

import pl.lewapek.workshop.observability.config.VariantConfig
import zio.*
import zio.json.*

case class WithVariant[T](variant: VariantConfig, value: T)

object WithVariant:
  given [T: JsonEncoder]: JsonEncoder[WithVariant[T]] = JsonEncoder.derived
  given [T: JsonDecoder]: JsonDecoder[WithVariant[T]] = JsonDecoder.derived

  extension [T](t: T) def withVariant(variantConfig: VariantConfig): WithVariant[T] = WithVariant(variantConfig, t)
end WithVariant
