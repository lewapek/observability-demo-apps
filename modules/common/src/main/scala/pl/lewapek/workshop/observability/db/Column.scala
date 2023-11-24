package pl.lewapek.workshop.observability.db

import doobie.Write
import doobie.implicits.*
import doobie.util.fragment.Fragment

class Column[Input, Field: Write](val name: String, val extract: Input => Field):
  val fragment: Fragment                   = Fragment.const(name)
  def setExtracted(input: Input): Fragment = fragment ++ fr"=" ++ fr"${extract(input)}"
  def set(value: Field): Fragment          = fragment ++ fr"=" ++ fr"$value"
  def sortAscending: Fragment              = fragment ++ fr"asc"
  def sortDescending: Fragment             = fragment ++ fr"desc"
end Column

trait ColumnSupport[Input]:
  def column[T](name: String, extractField: Input => T)(using w: Write[T]): Column[Input, T] =
    new Column[Input, T](name, extractField)
  end column
end ColumnSupport
