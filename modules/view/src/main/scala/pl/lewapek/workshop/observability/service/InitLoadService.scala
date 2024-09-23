package pl.lewapek.workshop.observability.service

import pl.lewapek.workshop.observability.AppError
import pl.lewapek.workshop.observability.Bootstrap.SttpBackendType
import pl.lewapek.workshop.observability.config.VariantConfig
import pl.lewapek.workshop.observability.metrics.TracingService.TracingHeaders
import pl.lewapek.workshop.observability.model.*
import pl.lewapek.workshop.observability.utils.ProductsList
import zio.*
import zio.telemetry.opentelemetry.tracing.Tracing

import java.time.Instant

class InitLoadService(
  variantConfig: VariantConfig,
  productServiceClient: ProductServiceClient,
  orderServiceClient: OrderServiceClient,
  tracing: Tracing
):

  import InitLoadService.*

  private def randomOme[A](as: IndexedSeq[A]): UIO[A] = Random.nextIntBounded(as.length).map(as.apply)
  private def randomMaxAtoB[T](a: Int, b: Int)(as: IndexedSeq[T]): UIO[Set[T]] =
    for
      n   <- Random.nextIntBetween(a, b)
      set <- ZIO.foreach(1 to n)(_ => randomOme(as)).map(_.toSet)
    yield set

  def initLoad(using TracingHeaders): IO[AppError, Unit] =
    val productIdsZIO = (1 to 10).map { _ =>
      for
        input <- randomOme(ProductsList.productsFunFacts).map { case (name, funFacts) =>
          ProductInfoInput(name, funFacts.headOption, funFacts.lift(1))
        }
        product <- productServiceClient.addProduct(input)
      yield product.value.id
    }
    for
      productIds <- ZIO.collectAllPar(productIdsZIO).withParallelism(5)
      ordersZIO = (1 to 5).map { _ =>
        for
          chosenProductIds <- randomMaxAtoB(2, 10)(productIds)
          remark           <- randomOme(remarks)
          nowEpochMilli    <- Clock.instant.map(_.toEpochMilli)
          date  <- Random.nextLongBetween(nowEpochMilli - oneYearMillis, nowEpochMilli).map(Instant.ofEpochMilli)
          order <- orderServiceClient.addOrder(OrderInput(chosenProductIds.toList, Some(remark), date))
        yield ()
      }
      _ <- ZIO.collectAllParDiscard(ordersZIO).withParallelism(5)
    yield ()
    end for
  end initLoad

end InitLoadService

object InitLoadService:
  def initLoad(using TracingHeaders): ZIO[InitLoadService, AppError, Unit] =
    ZIO.serviceWithZIO[InitLoadService](_.initLoad)

  val layer                       = ZLayer.fromFunction(InitLoadService(_, _, _, _))
  private val oneYearMillis: Long = 366.days.toMillis

  private val remarks = Vector(
    "Exciting package, anticipation rising!",
    "Fresh surprises en route!",
    "Delightful arrival expected soon!",
    "Hoping for pleasant surprises!",
    "Ready for a tasty experience!",
    "Can't wait for the arrival!",
    "Expecting delightful surprises ahead!",
    "Fresh and tasty excitement!"
  )
end InitLoadService
