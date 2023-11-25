package pl.lewapek.workshop.observability.metrics

import zio.Chunk
import zio.metrics.Metric

import java.time.temporal.ChronoUnit

object PrometheusMetrics:
  val countLiveness  = Metric.counter("liveness_counter")
  val countReadiness = Metric.counter("readiness_counter")
  val requestHandlerTimer =
    Metric.timer(
      name = "timer",
      chronoUnit = ChronoUnit.MILLIS,
      boundaries = createBoundaries
    )

  val asyncJobsInProgress = Metric.gauge("jobs_running")

  private def createBoundaries =
    Chunk
      .fromIterable(1 to 4)
      .foldLeft((Chunk.single(1.0), 2.0, 2)) { case ((chunk, start, step), _) =>
        (chunk ++ Chunk.iterate(start, 5)(_ + step), start * 10, step * 10)
      }
      ._1

end PrometheusMetrics
