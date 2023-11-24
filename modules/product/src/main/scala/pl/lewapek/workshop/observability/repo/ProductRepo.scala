package pl.lewapek.workshop.observability.repo

import cats.data.NonEmptyList
import doobie.*
import doobie.postgres.implicits.*
import pl.lewapek.workshop.observability.db.{ColumnSupport, GenericRepo}
import pl.lewapek.workshop.observability.model.{ProductId, ProductInfo, ProductInfoInput}

object ProductRepo
  extends GenericRepo[ProductId, ProductInfoInput, ProductInfo](
    "product_info",
    NonEmptyList.of(
      ProductColumns.name,
      ProductColumns.funFact,
      ProductColumns.additionalFunFact
    )
  )

object ProductColumns extends ColumnSupport[ProductInfoInput]:
  val name              = column("name", _.name)
  val funFact           = column("fun_fact", _.funFact)
  val additionalFunFact = column("additional_fun_fact", _.additionalFunFact)
end ProductColumns
