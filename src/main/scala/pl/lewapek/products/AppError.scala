package pl.lewapek.products

final case class AppError(code: String, message: String, cause: Throwable) extends Throwable(cause)

object AppError:

  def internalServerError(message: String): AppError =
    AppError("INTERNAL_SERVER_ERROR", message, null)

  def internalServerError(message: String, cause: Throwable): AppError =
    AppError("INTERNAL_SERVER_ERROR", message, cause)
  def badRequest(message: String, cause: Throwable): AppError =
    AppError("BAD_REQUEST", message, cause)
end AppError
