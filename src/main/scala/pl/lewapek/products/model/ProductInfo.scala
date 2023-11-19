package pl.lewapek.products.model

import zio.json.{JsonDecoder, JsonEncoder}

case class ProductInfo(id: String, name: String, description: Option[String]) derives JsonEncoder, JsonDecoder
