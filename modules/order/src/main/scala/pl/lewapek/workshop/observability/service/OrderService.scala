package pl.lewapek.workshop.observability.service

import cats.data.NonEmptyList
import doobie.Transactor
import pl.lewapek.workshop.observability.AppError
import pl.lewapek.workshop.observability.config.VariantConfig
import pl.lewapek.workshop.observability.db.ZTransaction
import pl.lewapek.workshop.observability.model.{Order, OrderId, OrderInput, ProductInfo}
import pl.lewapek.workshop.observability.repo.OrderRepo
import zio.*
import zio.telemetry.opentelemetry.tracing.Tracing

class OrderService(
  variantConfig: VariantConfig,
  transactor: Transactor[Task],
  tracing: Tracing
) extends ZTransaction(transactor):

  private val transform: Order => Order =
    if variantConfig.version == 1 then (p: Order) => p.copy(remarks = None)
    else identity

  extension (o: Order) private def transformToVersion: Order = transform(o)

  def add(input: OrderInput): IO[AppError, Order] =
    OrderRepo.insert(input).transactional.map(_.transformToVersion)

  def get(id: OrderId): IO[AppError, Option[Order]] =
    OrderRepo.select(id).transactional.map(_.map(_.transformToVersion))

  def all: IO[AppError, List[Order]] =
    OrderRepo.all.transactional.map(_.map(_.transformToVersion))

  def get(ids: NonEmptyList[OrderId]): IO[AppError, List[Order]] =
    OrderRepo.select(ids).transactional.map(_.map(_.transformToVersion))

  def get(ids: Chunk[OrderId]): IO[AppError, List[Order]] =
    NonEmptyList.fromList(ids.toList) match
      case None       => ZIO.succeed(List.empty)
      case Some(list) => OrderRepo.select(list).transactional.map(_.map(_.transformToVersion))
  end get

end OrderService

object OrderService:

  def add(input: OrderInput): ZIO[OrderService, AppError, Order] =
    ZIO.serviceWithZIO[OrderService](_.add(input))

  def get(id: OrderId): ZIO[OrderService, AppError, Option[Order]] =
    ZIO.serviceWithZIO[OrderService](_.get(id))

  def all: ZIO[OrderService, AppError, List[Order]] =
    ZIO.serviceWithZIO[OrderService](_.all)

  def get(ids: NonEmptyList[OrderId]): ZIO[OrderService, AppError, List[Order]] =
    ZIO.serviceWithZIO[OrderService](_.get(ids))

  def get(ids: Chunk[OrderId]): ZIO[OrderService, AppError, List[Order]] =
    ZIO.serviceWithZIO[OrderService](_.get(ids))

  val layer = ZLayer.fromFunction(OrderService(_, _, _))
end OrderService
