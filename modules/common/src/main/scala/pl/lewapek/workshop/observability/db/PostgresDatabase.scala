package pl.lewapek.workshop.observability.db

import com.zaxxer.hikari.HikariConfig
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import pl.lewapek.workshop.observability.config.DbConfig
import zio.*
import zio.interop.catz.*

object PostgresDatabase:
  private def migrate(config: DbConfig): Task[Unit] =
    ZIO.attempt {
      Flyway
        .configure()
        .dataSource(config.jdbcUrl, config.user, config.password)
        .load()
        .migrate()
    }.unit
  end migrate

  private def makeTransactor(config: DbConfig): RIO[Scope, Transactor[Task]] =
    val hikariConfig = HikariConfig()
    hikariConfig.setJdbcUrl(config.jdbcUrl)
    hikariConfig.setDriverClassName(config.driver)
    hikariConfig.setUsername(config.user)
    hikariConfig.setPassword(config.password)
    hikariConfig.setConnectionTimeout(config.connectionTimeout)
    hikariConfig.setMinimumIdle(config.minimumIdle)
    hikariConfig.setMaximumPoolSize(config.maximumPoolSize)

    HikariTransactor
      .fromHikariConfig[Task](hikariConfig)
      .toScopedZIO
  end makeTransactor

  val transactorLive: RLayer[DbConfig, Transactor[Task]] =
    ZLayer
      .scoped(
        for
          _          <- ZIO.logDebug("Constructing layer PostgresDatabase")
          config     <- ZIO.service[DbConfig]
          _          <- migrate(config)
          transactor <- makeTransactor(config)
        yield transactor
      )

end PostgresDatabase
