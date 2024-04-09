package pl.lewapek.workshop.observability.metrics

import zio.Chunk
import zio.metrics.{Metric, MetricLabel}

import java.time.temporal.ChronoUnit
import zio.*

object PrometheusMetrics:
  private val commonLabels = Set(
    MetricLabel("color", "blue"),
    MetricLabel("country", "PL")
  )

  val countLiveness  = Metric.counter("liveness_counter").tagged(commonLabels)
  val countReadiness = Metric.counter("readiness_counter").tagged(commonLabels)
  val requestHandlerTimer =
    Metric
      .timer(
        name = "workshop_request_timer",
        chronoUnit = ChronoUnit.MILLIS,
        boundaries = createBoundaries
      )
      .tagged(commonLabels)

  val asyncJobsInProgress = Metric.gauge("jobs_running").tagged(commonLabels).tagged("useless", "value")
  val asyncJobsFinished = Metric.counter("jobs_finished").tagged(commonLabels)

  val sampleSummary = Metric
    .summary(
      "sample_summary",
      maxAge = 2.hours,
      maxSize = 100,
      error = 0.03d,
      quantiles = Chunk(0.1, 0.5, 0.9)
    )
    .tagged(commonLabels)

  private def createBoundaries =
    Chunk
      .fromIterable(1 to 3)
      .foldLeft((Chunk.single(10.0), 20.0, 20)) { case ((chunk, start, step), _) =>
        (chunk ++ Chunk.iterate(start, 5)(_ + step), start * 10, step * 10)
      }
      ._1

end PrometheusMetrics
