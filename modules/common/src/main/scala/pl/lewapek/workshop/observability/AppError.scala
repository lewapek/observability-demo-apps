package pl.lewapek.workshop.observability

final case class AppError(name: String, message: String, cause: Option[Throwable]) extends Throwable:
  def show: String = s"$name: $message" + cause.fold("")(c => s", cause: $c")
end AppError

object AppError:
  def internal(message: String): AppError =
    AppError("internal", message, None)
  def internal(message: String, cause: Throwable): AppError =
    val error = AppError("internal", message, Some(cause))
    error.initCause(cause)
    error
  end internal
end AppError
