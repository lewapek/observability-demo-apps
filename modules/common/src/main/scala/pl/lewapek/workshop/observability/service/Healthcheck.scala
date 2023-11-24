package pl.lewapek.workshop.observability.service

import zio.json.{DeriveJsonEncoder, JsonEncoder}
import zio.prelude.Identity
import zio.telemetry.opentelemetry.tracing.Tracing
import zio.{Chunk, ZIO}

sealed trait Healthcheck[-R]:
  self =>
  def run: ZIO[R, Nothing, Chunk[Healthcheck.NamedStatus]]

  def ++[R1 <: R](other: Healthcheck[R1]): Healthcheck[R1] = new Healthcheck[R1]:
    def run: ZIO[R1, Nothing, Chunk[Healthcheck.NamedStatus]] = self.run.zipWith(other.run)(_ ++ _)

end Healthcheck

object Healthcheck:
  val empty: Healthcheck[Tracing] = new Healthcheck[Tracing]:
    def run: ZIO[Tracing, Nothing, Chunk[NamedStatus]] = ZIO.serviceWithZIO[Tracing] { tracing =>
      (ZIO.succeed(Chunk.empty) @@ tracing.aspects.span("inner")).delay(zio.Duration.fromMillis(33)) @@ tracing.aspects
        .span("middle")
    }

  def apply[R](name: String, check: ZIO[R, Nothing, Status]): Healthcheck[R] = new Healthcheck[R]:
    def run: ZIO[R, Nothing, Chunk[NamedStatus]] = check.map(result => Chunk.single(NamedStatus(name, result)))

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
