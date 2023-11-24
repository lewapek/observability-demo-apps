package pl.lewapek.workshop.observability.model

import pl.lewapek.workshop.observability.types.NewPowerType
import zio.json.{JsonDecoder, JsonEncoder}

import java.time.Instant

final case class Order(id: OrderId, products: List[ProductId], remarks: Option[String], date: Instant)
  derives JsonEncoder,
    JsonDecoder
final case class OrderInput(products: List[ProductId], remarks: Option[String], date: Instant)
  derives JsonEncoder,
    JsonDecoder

object OrderId extends NewPowerType[Long]
type OrderId = OrderId.Type
