package pl.lewapek.workshop.observability.service

import pl.lewapek.workshop.observability.AppError
import pl.lewapek.workshop.observability.config.KafkaConfig
import pl.lewapek.workshop.observability.model.ProductInfoInput
import pl.lewapek.workshop.observability.utils.ProductsList
import zio.*
import zio.kafka.consumer.{Consumer, Subscription}
import zio.kafka.serde.Serde
import zio.stream.ZStream

class ProductsConsumer(config: KafkaConfig, consumer: Consumer, productsClient: ProductServiceClient):

  def consume: ZStream[Any, AppError, Nothing] =
    Consumer
      .plainStream(Subscription.topics(config.productsTopic), Serde.string, Serde.string)
      .tap { record =>
        for
          _             <- ZIO.logInfo(s"Looking for fun facts for ${record.value}")
          maybeFunFacts <- findFunFacts(record.value)
          input = ProductInfoInput(record.value, maybeFunFacts.flatMap(_._1), maybeFunFacts.flatMap(_._2))
          _ <- ZIO.logInfo(s"Trying to update product with input: $input")
          _ <- productsClient.updateFunFacts(input)
        yield ()
      }
      .map(_.offset)
      .aggregateAsync(Consumer.offsetBatches)
      .mapZIO(_.commit)
      .mapError(AppError.internal("Problem with consumer stream", _))
      .drain
      .provideEnvironment(ZEnvironment(consumer))

  private def findFunFacts(name: String): UIO[Option[(Option[String], Option[String])]] =
    for
      sleepTimeMillis <- Random.nextIntBetween(1000, 5000)
      _               <- ZIO.sleep(sleepTimeMillis.millis)
      maybeAllFunFacts = ProductsList.lowercaseProductsToFunFacts.get(name.toLowerCase)
      result <- ZIO.foreach(maybeAllFunFacts)(twoRandomFunFacts)
    yield result

  private def twoRandomFunFacts(vector: Vector[String]): UIO[(Option[String], Option[String])] =
    Random
      .shuffle(vector.toList)
      .map(shuffled => shuffled.headOption -> shuffled.lift(1))

end ProductsConsumer

object ProductsConsumer:
  val layer = ZLayer.fromFunction(ProductsConsumer(_, _, _))

  def consume: ZStream[ProductsConsumer, AppError, Nothing] =
    ZStream.serviceWithStream[ProductsConsumer](_.consume)
end ProductsConsumer
