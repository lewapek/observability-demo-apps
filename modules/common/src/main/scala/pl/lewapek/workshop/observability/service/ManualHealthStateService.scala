package pl.lewapek.workshop.observability.service

import zio.*

class ManualHealthStateService(state: Ref[Boolean]):
  def isHealthy: UIO[Boolean] = state.get
  def setHealthy: UIO[Unit]   = state.set(true) <* ZIO.logInfo("Manual state set as healthy")
  def setUnhealthy: UIO[Unit] = state.set(false) <* ZIO.logInfo("Manual state set as unhealthy")

end ManualHealthStateService

object ManualHealthStateService:
  val layer: ZLayer[Any, Nothing, ManualHealthStateService] =
    ZLayer.fromZIO(Ref.make(true).map(ManualHealthStateService(_)))

  def isHealthy: URIO[ManualHealthStateService, Boolean] = ZIO.serviceWithZIO(_.isHealthy)
  def setHealthy: URIO[ManualHealthStateService, Unit]   = ZIO.serviceWithZIO(_.setHealthy)
  def setUnhealthy: URIO[ManualHealthStateService, Unit] = ZIO.serviceWithZIO(_.setUnhealthy)
end ManualHealthStateService
