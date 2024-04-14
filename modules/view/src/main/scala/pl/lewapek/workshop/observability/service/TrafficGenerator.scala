package pl.lewapek.workshop.observability.service

import pl.lewapek.workshop.observability.AppError
import pl.lewapek.workshop.observability.Bootstrap.SttpBackendType
import pl.lewapek.workshop.observability.config.VariantConfig
import pl.lewapek.workshop.observability.metrics.TracingService.TracingHeaders
import pl.lewapek.workshop.observability.model.*
import zio.*
import zio.json.{JsonDecoder, JsonEncoder}
import zio.telemetry.opentelemetry.tracing.Tracing

import java.time.Instant

class TrafficGenerator(
  fiberRef: Ref[Option[Fiber.Runtime[AppError, Unit]]],
  productServiceClient: ProductServiceClient,
  orderServiceClient: OrderServiceClient
):

  import TrafficGenerator.*
  private val schedule = Schedule.spaced(15.second).jittered(0.5, 2.0)

  def start: UIO[Status] =
    for
      _          <- ZIO.logInfo("Starting async jobs generator")
      maybeFiber <- fiberRef.get
      status <- maybeFiber.fold(
        run.forkDaemon.flatMap(forked => fiberRef.set(Some(forked))).as(Status.Started)
      ) {
        _.status.flatMap { fiberStatus =>
          if fiberStatus.isDone then run.forkDaemon.flatMap(forked => fiberRef.set(Some(forked))).as(Status.Started)
          else ZIO.succeed(Status.AlreadyStarted)
        }
      }
    yield status

  def status: UIO[Status] =
    for
      maybeFiber <- fiberRef.get
      result <- maybeFiber.fold(ZIO.succeed(Status.Stopped)) { fiber =>
        for
          fiberStatus <- fiber.status
          status <-
            if fiberStatus.isDone then fiberRef.set(None).as(Status.Stopped)
            else ZIO.succeed(Status.Started)
        yield status
      }
    yield result

  def stop: UIO[Status] =
    for
      _          <- ZIO.logInfo("Stopping async jobs generator")
      maybeFiber <- fiberRef.get
      status <- maybeFiber.fold(
        ZIO.succeed(Status.AlreadyStopped)
      )(_.interrupt *> fiberRef.set(None).as(Status.Stopped))
    yield status

  private def run: IO[AppError, Unit] =
    ZIO
      .collectAllParDiscard(
        List(
          ZIO.logInfo("Sending job to product") *> productServiceClient.asyncJob.catchAll(logSendIssue),
          ZIO.logInfo("Sending job to order") *> orderServiceClient.asyncJob.catchAll(logSendIssue)
        )
      )
      .withParallelism(2)
      .repeat(schedule)
      .unit

  private def logSendIssue(e: AppError) = ZIO.logWarning(s"Error sending request: ${e.show}")

end TrafficGenerator

object TrafficGenerator:

  def layer = ZLayer.fromZIO(
    for
      ref     <- Ref.make(None)
      product <- ZIO.service[ProductServiceClient]
      order   <- ZIO.service[OrderServiceClient]
    yield TrafficGenerator(ref, product, order)
  )

  def start: URIO[TrafficGenerator, Status] =
    ZIO.serviceWithZIO(_.start)

  def status: URIO[TrafficGenerator, Status] =
    ZIO.serviceWithZIO(_.status)

  def stop: URIO[TrafficGenerator, Status] =
    ZIO.serviceWithZIO(_.stop)

  enum Status:
    case Started, Stopped, AlreadyStarted, AlreadyStopped
  end Status
  object Status:
    given JsonEncoder[Status] = JsonEncoder[String].contramap(_.toString)
  end Status

end TrafficGenerator
