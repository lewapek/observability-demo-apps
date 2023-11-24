package pl.lewapek.workshop.observability.db

import cats.data.NonEmptyList
import cats.syntax.foldable.*
import doobie.*
import doobie.implicits.*
import doobie.util.fragment.Fragment

trait GenericRepo[PrimaryId, Input, Model](
  tableName: String,
  columnsTail: NonEmptyList[Column[Input, ?]]
)(using
  Put[PrimaryId],
  Write[Input],
  Read[Model]
):
  val tableFr  = Fragment.const(tableName)
  val idColumn = "id"
  val idFr     = Fragment.const(idColumn)

  val allColumnsNames = (idColumn :: columnsTail.map(_.name)).toList

  val noIdFr = columnsTail.map(_.fragment).intercalate(fr",")
  val allFr  = idFr ++ fr"," ++ noIdFr

  def insert(input: Input): ConnectionIO[Model] =
    fr"insert into $tableFr ($noIdFr) values ($input)".update
      .withUniqueGeneratedKeys[Model](allColumnsNames*)

  def replace(id: PrimaryId, input: Input): ConnectionIO[Int] =
    val setAll = columnsTail.map(_.setExtracted(input)).intercalate(fr",")
    fr"update $tableFr set $setAll where $idFr = $id".update.run
  end replace

  def update(id: PrimaryId, setFragment: Fragment, otherSetFragments: Fragment*): ConnectionIO[Int] =
    val set = (setFragment :: otherSetFragments.toList).intercalate(fr",")
    fr"update $tableFr set $set where $idFr = $id".update.run
  end update

  def select(id: PrimaryId): ConnectionIO[Option[Model]] =
    fr"select $allFr from $tableFr where $idFr = $id".query[Model].option

  def select(params: SelectParams): ConnectionIO[List[Model]] =
    queryHelper(params).to[List]

  def all: ConnectionIO[List[Model]] =
    select(SelectParams.empty)

  def select(ids: NonEmptyList[PrimaryId]): ConnectionIO[List[Model]] =
    fr"select $allFr from $tableFr where ${Fragments.in(idFr, ids)}".query[Model].to[List]

  def delete(id: PrimaryId): ConnectionIO[Int] =
    fr"delete from $tableFr where $idFr = $id".update.run

  private def queryHelper(params: SelectParams) =
    val where  = params.query.fold(Fragment.empty)(fr"where" ++ _)
    val sort   = if params.sort.isEmpty then Fragment.empty else fr"order by" ++ params.sort.intercalate(fr",")
    val offset = params.offset.fold(Fragment.empty)(value => fr"offset $value")
    val limit  = params.limit.fold(Fragment.empty)(value => fr"limit $value")
    val whole  = fr"select $allFr from $tableFr" ++ where ++ sort ++ offset ++ limit
    whole.query[Model]
  end queryHelper
end GenericRepo
