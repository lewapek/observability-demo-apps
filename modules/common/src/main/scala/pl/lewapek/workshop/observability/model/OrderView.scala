package pl.lewapek.workshop.observability.model

import zio.json.{JsonDecoder, JsonEncoder}

import java.time.Instant

final case class OrderView(id: OrderId, products: List[ProductInfo], remarks: Option[String], date: Instant)
  derives JsonEncoder,
    JsonDecoder
