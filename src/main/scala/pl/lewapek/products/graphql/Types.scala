package pl.lewapek.products.graphql

import java.util.UUID
import caliban.schema.{ArgBuilder, Schema}

object Types:
  final case class RecurrentQueryArgs(ttl: Int, sleepSecondsBefore: Int, sleepSecondsAfter: Int)
  object RecurrentQueryArgs:
    given ArgBuilder[RecurrentQueryArgs]  = ArgBuilder.gen
    given Schema[Any, RecurrentQueryArgs] = Schema.gen

  final case class Report(id: UUID)
  object Report:
    given Schema[Any, Report] = Schema.gen
end Types
