package pl.lewapek.workshop.observability.model

import pl.lewapek.workshop.observability.types.NewPowerType
import zio.json.{JsonDecoder, JsonEncoder}

case class ProductInfo(id: ProductId, name: String, funFact: Option[String], additionalFunFact: Option[String])
  derives JsonEncoder,
    JsonDecoder
case class ProductInfoInput(name: String, funFact: Option[String], additionalFunFact: Option[String])
  derives JsonEncoder,
    JsonDecoder

object ProductId extends NewPowerType[Long]
type ProductId = ProductId.Type
