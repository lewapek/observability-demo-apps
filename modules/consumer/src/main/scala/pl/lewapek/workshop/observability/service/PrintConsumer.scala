package pl.lewapek.workshop.observability.service

import pl.lewapek.workshop.observability.AppError
import pl.lewapek.workshop.observability.config.KafkaConfig
import pl.lewapek.workshop.observability.metrics.TracingService.TracingHeaders
import zio.*
import zio.kafka.consumer.{Consumer, Subscription}
import zio.kafka.serde.Serde
import zio.stream.ZStream

class PrintConsumer(config: KafkaConfig, consumer: Consumer):

  private given TracingHeaders = TracingHeaders.empty

  def consume: ZStream[Any, AppError, Nothing] =
    Consumer
      .plainStream(Subscription.topics(config.printTopic), Serde.string, Serde.string)
      .tap { record =>
        for
          sleepTimeMillis <- Random.nextIntBetween(1000, 5000)
          _               <- ZIO.sleep(sleepTimeMillis.millis)
          _               <- ZIO.logInfo(s"Got message ${record.value}. Sleeping for $sleepTimeMillis millis")
        yield ()
      }
      .map(_.offset)
      .aggregateAsync(Consumer.offsetBatches)
      .mapZIO(_.commit)
      .mapError(AppError.internal("Problem with consumer stream", _))
      .drain
      .provideEnvironment(ZEnvironment(consumer))

end PrintConsumer

object PrintConsumer:
  val layer = ZLayer.fromFunction(PrintConsumer(_, _))

  def consume: ZStream[PrintConsumer, AppError, Nothing] =
    ZStream.serviceWithStream[PrintConsumer](_.consume)
end PrintConsumer
