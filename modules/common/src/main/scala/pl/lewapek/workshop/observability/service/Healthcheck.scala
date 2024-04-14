package pl.lewapek.workshop.observability.service

import doobie.util.transactor.Transactor
import zio.json.{DeriveJsonEncoder, JsonEncoder}
import zio.prelude.Identity
import zio.telemetry.opentelemetry.tracing.Tracing
import zio.{Chunk, Task, ZIO}
import doobie.*
import doobie.implicits.*
import zio.interop.catz.*
import zio.*

sealed trait Healthcheck[-R]:
  self =>
  def run: ZIO[R, Nothing, Chunk[Healthcheck.NamedStatus]]

  def ++[R1 <: R](other: Healthcheck[R1]): Healthcheck[R1] = new Healthcheck[R1]:
    def run: ZIO[R1, Nothing, Chunk[Healthcheck.NamedStatus]] = self.run.zipWith(other.run)(_ ++ _).withParallelism(5)

end Healthcheck

object Healthcheck:
  val empty: Healthcheck[Any] = new Healthcheck[Any]:
    def run: UIO[Chunk[NamedStatus]] = ZIO.succeed(Chunk.empty)

  def apply[R](name: String, check: ZIO[R, Nothing, Status]): Healthcheck[R] = new Healthcheck[R]:
    def run: ZIO[R, Nothing, Chunk[NamedStatus]] = check.map(result => Chunk.single(NamedStatus(name, result)))

  def manualToggle: Healthcheck[ManualHealthStateService] =
    Healthcheck(
      "health-toggle",
      ManualHealthStateService.isHealthy.map(isHealthy => if isHealthy then Status.Ok else Status.Error)
    )

  val postgres: Healthcheck[Transactor[Task]] =
    Healthcheck(
      "postgres",
      for
        transactor <- ZIO.service[Transactor[Task]]
        check <- fr"SELECT 1"
          .query[Int]
          .unique
          .transact(transactor)
          .orDie
      yield
        if check == 1 then Healthcheck.Status.Ok
        else Healthcheck.Status.Error
    )
  end postgres
  final case class NamedStatus(name: String, status: Status) derives JsonEncoder

  enum Status:
    self =>
    case Ok, Error
    lazy val isOk: Boolean = fold(ok = true, error = false)

    def &&(that: Status): Status = fold(ok = that, error = self)
    def fold[A](ok: => A, error: => A): A = self match
      case Status.Ok    => ok
      case Status.Error => error
  end Status
  object Status:
    given JsonEncoder[Status] = JsonEncoder.string.contramap[Status](_.toString.toLowerCase)
    given Identity[Status]    = Identity.make(Status.Ok, _ && _)
  end Status

end Healthcheck
