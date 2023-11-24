package pl.lewapek.workshop.observability.types

import cats.Show
import doobie.{Meta, Read, Write}
import zio.json.{JsonDecoder, JsonEncoder}
import zio.prelude.Newtype

trait Typeclasses[T]:
  self: Newtype[T] =>
  // doobie
  given (using Meta[T]): Meta[self.Type]                  = self.derive
  given (using m: Write[List[T]]): Write[List[self.Type]] = m.asInstanceOf[Write[List[self.Type]]]
  given (using m: Read[List[T]]): Read[List[self.Type]]   = m.asInstanceOf[Read[List[self.Type]]]

  // circe
  given (using JsonEncoder[T]): JsonEncoder[self.Type] = self.derive
  given (using JsonDecoder[T]): JsonDecoder[self.Type] = self.derive

  given (using Show[T]): Show[self.Type] = self.derive
end Typeclasses

trait NewPowerType[T] extends Newtype[T] with Typeclasses[T]

object Offset extends NewPowerType[Long]:
  def from(s: String): Option[Offset] = s.toLongOption.map(Offset(_))
end Offset
type Offset = Offset.Type

object Limit extends NewPowerType[Long]:
  def from(s: String): Option[Limit] = s.toLongOption.map(Limit(_))
end Limit
type Limit = Limit.Type
