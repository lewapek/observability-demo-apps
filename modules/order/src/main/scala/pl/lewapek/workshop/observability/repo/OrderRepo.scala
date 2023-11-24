package pl.lewapek.workshop.observability.repo

import cats.data.NonEmptyList
import doobie.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import pl.lewapek.workshop.observability.db.{ColumnSupport, GenericRepo}
import pl.lewapek.workshop.observability.model.{Order, OrderId, OrderInput, ProductId}

object OrderRepo
  extends GenericRepo[OrderId, OrderInput, Order](
    "order_info",
    NonEmptyList.of(
      OrderColumns.products,
      OrderColumns.remarks,
      OrderColumns.date
    )
  )

object OrderColumns extends ColumnSupport[OrderInput]:
  val products = column("products", _.products)
  val remarks  = column("remarks", _.remarks)
  val date     = column("order_date", _.date)
end OrderColumns
