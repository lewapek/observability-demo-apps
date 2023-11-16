inThisBuild(
  List(
    name         := "products-management",
    organization := "pl.lewapek",
    scalaVersion := "3.3.1",
    version      := IO.read(file("version")).linesIterator.next()
  )
)

lazy val root = (project in file("."))
  .settings(
    libraryDependencies ++= Dependencies.all,
    scalacOptions -= "-Wunused:imports",
  )
  .settings(
    mainClass := Some("pl.lewapek.products.Main"),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", _)                         => MergeStrategy.discard
      case x if x.contains("io.netty.versions.properties") => MergeStrategy.first
      case x if x.contains("rally-version.properties")     => MergeStrategy.discard
      case x if x.endsWith("module-info.class")            => MergeStrategy.discard
      case x if x.endsWith(":LICENSE")                     => MergeStrategy.discard
      case x if x.endsWith(":NOTICE")                      => MergeStrategy.discard
      case x if x.endsWith(".config")                      => MergeStrategy.discard
      case x if x.endsWith(".json")                        => MergeStrategy.first
      case x if Assembly.isConfigFile(x) =>
        MergeStrategy.concat
      case x =>
        MergeStrategy.first

    },
    assembly / logLevel        := Level.Info,
    assembly / assemblyJarName := s"app-assembly-${version.value}.jar",
    assembly / test            := {},
    assembly / assemblyOutputPath := {
      (assembly / baseDirectory).value / "target" / (assembly / assemblyJarName).value
    }
  )
  .enablePlugins(AssemblyPlugin)

addCommandAlias("fmt", "all scalafmtSbt scalafmtAll")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheckAll")

addCommandAlias("fix", ";scalafixAll; test:scalafixAll; scalafmtAll; scalafmtSbt")
addCommandAlias("fixCheck", ";test:compile ;scalafixAll --check ;scalafmtCheckAll; scalafmtSbtCheck")
