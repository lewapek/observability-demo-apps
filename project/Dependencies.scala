import sbt.*

// this should remain scala2 ==> disabling scala3 rewrite rules here
// scalafmt: { rewrite.scala3.convertToNewSyntax = false, rewrite.scala3.removeOptionalBraces = false }
object Dependencies {

  private val zioVersion            = "2.1.14"
  private val zioInteropCatsVersion = "23.1.0.0"
  private val zioLoggingVersion     = "2.1.14"
  private val zioJsonVersion        = "0.6.2"
  private val zioConfigVersion      = "4.0.0-RC16"
  private val zioMetricsVersion     = "2.1.0"
  private val zioKafkaVersion       = "2.7.4"
  private val sttpVersion           = "3.10.2"
  private val ducktapeVersion       = "0.1.11"
  private val tapirVersion          = "1.3.0"
  private val doobieVersion         = "1.0.0-RC5"

  private val zio = Seq(
    "dev.zio" %% "zio"         % zioVersion,
    "dev.zio" %% "zio-streams" % zioVersion
  )

  private val zioTest = Seq(
    "dev.zio" %% "zio-test"     % zioVersion % Test,
    "dev.zio" %% "zio-test-sbt" % zioVersion % Test
  )

  private val zioInteropCats = Seq(
    "dev.zio" %% "zio-interop-cats" % zioInteropCatsVersion
  )

  private val zioLogging = Seq(
    "dev.zio"  %% "zio-logging"              % zioLoggingVersion,
    "dev.zio"  %% "zio-logging-slf4j-bridge" % zioLoggingVersion,
    "org.slf4j" % "slf4j-api"                % "1.7.36" // must match zio logging's version
  )

  private val zioJson = Seq(
    "dev.zio" %% "zio-json"        % zioJsonVersion,
    "dev.zio" %% "zio-json-golden" % zioJsonVersion % Test
  )

  private val zioConfig = Seq(
    "dev.zio" %% "zio-config"          % zioConfigVersion,
    "dev.zio" %% "zio-config-magnolia" % zioConfigVersion,
    "dev.zio" %% "zio-config-typesafe" % zioConfigVersion
  )

  private val zioMetrics = Seq(
    "dev.zio" %% "zio-metrics-connectors"            % zioMetricsVersion,
    "dev.zio" %% "zio-metrics-connectors-prometheus" % zioMetricsVersion
  )

  private val openTelemetry = Seq(
    "io.opentelemetry" % "opentelemetry-exporter-otlp" % "1.29.0",
    "io.opentelemetry" % "opentelemetry-sdk"           % "1.29.0"
  )

  private val zioOpenTelemetry = Seq(
    "dev.zio" %% "zio-opentelemetry" % "3.0.0-RC17"
  )

  private val zioCache = Seq(
    "dev.zio" %% "zio-cache" % "0.2.3"
  )

  private val zioPrelude = Seq(
    "dev.zio" %% "zio-prelude" % "1.0.0-RC20"
  )
  private val zioHttp = Seq(
    "dev.zio" %% "zio-http" % "3.0.0-RC1"
  )

  private val zioSttp = Seq(
    "com.softwaremill.sttp.client3" %% "zio"                % sttpVersion,
    "com.softwaremill.sttp.client3" %% "zio-json"           % sttpVersion,
    "com.softwaremill.sttp.client3" %% "prometheus-backend" % sttpVersion
  )

  private val zioKafka = Seq(
    "dev.zio" %% "zio-kafka" % zioKafkaVersion
  )

  private val tapir = Seq(
    "com.softwaremill.sttp.tapir" %% "tapir-json-zio"       % tapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-jsoniter-scala" % tapirVersion
  )

  private val ducktape = Seq(
    "io.github.arainko" %% "ducktape" % ducktapeVersion
  )

  private val doobie = Seq(
    "org.tpolecat" %% "doobie-core"     % doobieVersion,
    "org.tpolecat" %% "doobie-postgres" % doobieVersion,
    "org.tpolecat" %% "doobie-hikari"   % doobieVersion
  )

  val flyway = Seq(
    "org.flywaydb" % "flyway-core" % "9.11.0"
  )

  val common = Seq(
    zio,
    zioInteropCats,
    zioTest,
    zioLogging,
    zioJson,
    zioConfig,
    zioMetrics,
    openTelemetry,
    zioOpenTelemetry,
    zioCache,
    zioPrelude,
    zioHttp,
    zioSttp,
    zioKafka,
    tapir,
    ducktape,
    doobie,
    flyway
  ).flatten

  val kafka = zioKafka
}
