import sbtassembly.AssemblyPlugin.autoImport.assembly

inThisBuild(
  List(
    organization := "pl.lewapek",
    scalaVersion := "3.4.0",
    version      := IO.read(file("version")).linesIterator.next()
  )
)

lazy val assemblySettings = Seq(
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

lazy val common = (project in file("modules/common"))
  .settings(
    libraryDependencies ++= Dependencies.all,
    scalacOptions -= "-Wunused:imports"
  )
  .disablePlugins(AssemblyPlugin)

lazy val product = (project in file("modules/product"))
  .settings(
    name := "products",
    scalacOptions -= "-Wunused:imports",
  )
  .dependsOn(common)
  .settings(assemblySettings)
  .enablePlugins(AssemblyPlugin)

lazy val order = (project in file("modules/order"))
  .settings(
    name := "orders",
    scalacOptions -= "-Wunused:imports",
  )
  .dependsOn(common)
  .settings(assemblySettings)
  .enablePlugins(AssemblyPlugin)

lazy val view = (project in file("modules/view"))
  .settings(
    name := "orders",
    scalacOptions -= "-Wunused:imports",
  )
  .dependsOn(common)
  .settings(assemblySettings)
  .enablePlugins(AssemblyPlugin)

addCommandAlias("fmt", "all scalafmtSbt scalafmtAll")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheckAll")

addCommandAlias("fix", ";scalafixAll; test:scalafixAll; scalafmtAll; scalafmtSbt")
addCommandAlias("fixCheck", ";test:compile ;scalafixAll --check ;scalafmtCheckAll; scalafmtSbtCheck")
