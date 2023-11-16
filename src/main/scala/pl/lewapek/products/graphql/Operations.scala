package pl.lewapek.products.graphql

import caliban.schema.Schema
import pl.lewapek.products.graphql.Types.*
import zio.*
object Operations:
  final case class Query(
      recurrent: RecurrentQueryArgs => UIO[Int],
      products: UIO[Report]
  )
  object Query:
    given Schema[Any, Query] = Schema.gen

  final case class Mutation(
      addProduct: UIO[Long]
  )
  object Mutation:
    given Schema[Any, Mutation] = Schema.gen

end Operations
