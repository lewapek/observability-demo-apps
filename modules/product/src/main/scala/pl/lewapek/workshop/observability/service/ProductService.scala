package pl.lewapek.workshop.observability.service

import cats.data.NonEmptyList
import doobie.Transactor
import pl.lewapek.workshop.observability.AppError
import pl.lewapek.workshop.observability.config.VariantConfig
import pl.lewapek.workshop.observability.db.ZTransaction
import pl.lewapek.workshop.observability.model.{ProductId, ProductInfo, ProductInfoInput}
import pl.lewapek.workshop.observability.repo.ProductRepo
import zio.*
import zio.telemetry.opentelemetry.tracing.Tracing

class ProductService(transactor: Transactor[Task], tracing: Tracing, variantConfig: VariantConfig)
  extends ZTransaction(transactor):

  private val transform: ProductInfo => ProductInfo =
    if variantConfig.version == 1 then (p: ProductInfo) => p.copy(funFact = None, additionalFunFact = None)
    else if variantConfig.version == 2 then (p: ProductInfo) => p.copy(additionalFunFact = None)
    else identity

  extension (p: ProductInfo) private def transformToVersion: ProductInfo = transform(p)

  def add(input: ProductInfoInput): IO[AppError, ProductInfo] =
    ProductRepo.insert(input).transactional.map(_.transformToVersion)

  def get(id: ProductId): IO[AppError, Option[ProductInfo]] =
    ProductRepo.select(id).transactional.map(_.map(_.transformToVersion))

  def all: IO[AppError, List[ProductInfo]] =
    ProductRepo.all.transactional.map(_.map(_.transformToVersion))

  def get(ids: NonEmptyList[ProductId]): IO[AppError, List[ProductInfo]] =
    ProductRepo.select(ids).transactional.map(_.map(_.transformToVersion))

  def get(ids: Chunk[ProductId]): IO[AppError, List[ProductInfo]] =
    NonEmptyList.fromList(ids.toList) match
      case None       => ZIO.succeed(List.empty)
      case Some(list) => ProductRepo.select(list).transactional.map(_.map(_.transformToVersion))
  end get

end ProductService

object ProductService:
  def add(input: ProductInfoInput): ZIO[ProductService, AppError, ProductInfo] =
    ZIO.serviceWithZIO[ProductService](_.add(input))

  def get(id: ProductId): ZIO[ProductService, AppError, Option[ProductInfo]] =
    ZIO.serviceWithZIO[ProductService](_.get(id))

  def all: ZIO[ProductService, AppError, List[ProductInfo]] =
    ZIO.serviceWithZIO[ProductService](_.all)

  def get(ids: NonEmptyList[ProductId]): ZIO[ProductService, AppError, List[ProductInfo]] =
    ZIO.serviceWithZIO[ProductService](_.get(ids))

  def get(ids: Chunk[ProductId]): ZIO[ProductService, AppError, List[ProductInfo]] =
    ZIO.serviceWithZIO[ProductService](_.get(ids))

  val layer = ZLayer.fromFunction(ProductService(_, _, _))
end ProductService
