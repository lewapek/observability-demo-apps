package pl.lewapek.workshop.observability.db

import doobie.ConnectionIO
import doobie.implicits.*
import doobie.util.transactor.Transactor
import pl.lewapek.workshop.observability.AppError
import zio.interop.catz.*
import zio.{IO, Task}

trait ZTransaction(transactor: Transactor[Task]):
  extension [T](io: ConnectionIO[T])
    def transactional: IO[AppError, T] =
      io.transact(transactor).mapError(e => AppError.internal(s"DB error: ${e.getMessage}", e))
  end extension
end ZTransaction
