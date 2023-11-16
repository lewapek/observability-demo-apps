package pl.lewapek.products

import zio.ZIO

object ZIOUtils:
  extension [A](option: Option[A])
    def whenDefinedDiscard[R, E](f: A => ZIO[R, E, Unit]): ZIO[R, E, Unit] = option.fold(ZIO.unit)(f)
  end extension
end ZIOUtils
