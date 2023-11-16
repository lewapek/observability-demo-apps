import sbt.*

object Dependencies {

  private val zioVersion        = "2.0.16"
  private val zioLoggingVersion = "2.1.14"
  private val zioJsonVersion    = "0.6.1"
  private val zioConfigVersion  = "4.0.0-RC16"
  private val zioMetricsVersion = "2.1.0"
  private val calibanVersion    = "2.3.0"
  private val sttpVersion       = "3.8.11"
  private val ducktapeVersion   = "0.1.11"
  private val tapirVersion      = "1.3.0"

  private val zio = Seq(
    "dev.zio" %% "zio"         % zioVersion,
    "dev.zio" %% "zio-streams" % zioVersion
  )

  private val zioTest = Seq(
    "dev.zio" %% "zio-test"     % zioVersion % Test,
    "dev.zio" %% "zio-test-sbt" % zioVersion % Test
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

  private val zioMetricsConnectors = Seq(
    "dev.zio" %% "zio-metrics-connectors"            % zioMetricsVersion,
    "dev.zio" %% "zio-metrics-connectors-prometheus" % zioMetricsVersion
  )
  private val openTelemetry = Seq(
    "io.opentelemetry" % "opentelemetry-exporter-otlp" % "1.29.0", // should match the ones in zio-metrics-connectors
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

  private val tapir = Seq(
    "com.softwaremill.sttp.tapir" %% "tapir-json-zio"       % tapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-jsoniter-scala" % tapirVersion
  )

  private val ducktape = Seq(
    "io.github.arainko" %% "ducktape" % ducktapeVersion
  )

  private val calibanServer = Seq(
    "com.github.ghostdogpr" %% "caliban"          % calibanVersion,
    "com.github.ghostdogpr" %% "caliban-zio-http" % calibanVersion
  )

  val all = Seq(
    zio,
    zioTest,
    zioLogging,
    zioJson,
    zioConfig,
    zioMetricsConnectors,
    openTelemetry,
    zioOpenTelemetry,
    zioCache,
    zioPrelude,
    zioHttp,
    zioSttp,
    tapir,
    ducktape,
    calibanServer
  ).flatten
}
