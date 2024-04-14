package pl.lewapek.workshop.observability.metrics

import zio.Chunk
import zio.metrics.{Metric, MetricLabel}

import java.time.temporal.ChronoUnit
import zio.*
import zio.metrics.MetricKeyType.Histogram.Boundaries
import zio.metrics.MetricState.Histogram

object PrometheusMetrics:
  private val commonLabels = Set(
    MetricLabel("color", "blue"),
    MetricLabel("workshop", "lewapek")
  )

  enum GetVariant:
    case Single, Batch, All
  end GetVariant
  object GetVariant:
    private inline def label(variant: GetVariant): MetricLabel = MetricLabel("get_variant", variant.toString)
    val labelSingle                                            = label(Single)
    val labelBatch                                             = label(Batch)
    val labelAll                                               = label(All)
  end GetVariant

  val countLiveness  = Metric.counter("liveness_count").tagged(commonLabels)
  val countReadiness = Metric.counter("readiness_count").tagged(commonLabels)

  val workshopOrderRequests   = Metric.counter("order_get_requests_total").tagged(commonLabels)
  val workshopProductRequests = Metric.counter("product_get_requests_total").tagged(commonLabels)

  val requestHandlerTimer =
    Metric
      .timer(
        name = "workshop_request_timer",
        chronoUnit = ChronoUnit.MILLIS,
        boundaries = createRequestsBoundaries
      )
      .tagged(commonLabels)

  val asyncJobsInProgress = Metric.gauge("jobs_running").tagged(commonLabels).tagged("useless", "value")
  val asyncJobsFinished   = Metric.counter("jobs_finished_count").tagged(commonLabels)
  val asyncJobsDurationSec = Metric
    .histogram("jobs_duration_sec", Boundaries.fromChunk(Chunk.fromIterable((30 to 120 by 15).map(_.doubleValue))))
    .tagged(commonLabels)

  val sampleSummary = Metric
    .summary(
      "sample_summary",
      maxAge = 10.minutes,
      maxSize = 100,
      error = 0.05d,
      quantiles = Chunk.fromIterable(1 to 9 map (_ / 10.0))
    )
    .tagged(commonLabels)

  private def createRequestsBoundaries =
    Chunk
      .fromIterable(1 to 3)
      .foldLeft((Chunk.single(10.0), 20.0, 20)) { case ((chunk, start, step), _) =>
        (chunk ++ Chunk.iterate(start, 5)(_ + step), start * 10, step * 10)
      }
      ._1

end PrometheusMetrics
