package pl.lewapek.products.metrics

import zio.metrics.Metric

object PrometheusMetrics:
  val countHealthcheck = Metric.counter("healthcheck_counter")

  val reportsInProgress = Metric.gauge("reports_running")
end PrometheusMetrics
