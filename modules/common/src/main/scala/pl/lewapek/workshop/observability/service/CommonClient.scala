package pl.lewapek.workshop.observability.service

import pl.lewapek.workshop.observability.AppError
import pl.lewapek.workshop.observability.http.SttpUtils
import pl.lewapek.workshop.observability.metrics.TracingService.TracingHeaders
import sttp.model.Uri
import zio.*

trait CommonClient(uri: Uri):
  self: SttpUtils =>

  def asyncJob: IO[AppError, Unit] =
    sendUnit(_.post(uri.addPath("common", "async-job")))

  def sleep(millis: Int): IO[AppError, Unit] =
    sendUnit(_.post(uri.addPath("common", "sleep", millis.toString)))
end CommonClient
