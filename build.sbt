import Dependencies._
import org.scalajs.linker.interface.ModuleSplitStyle
import scala.sys.process.Process

lazy val scalacVersion = "3.3.4"

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .in(file("./shared"))
  .enablePlugins(BuildInfoPlugin)
  .settings(commonSettings)
  .settings(
    organization := "org.plasmalabs",
    // sbt-BuildInfo plugin can write any (simple) data available in sbt at
    // compile time to a `case class BuildInfo` that it makes available at runtime.
    buildInfoKeys := Seq[BuildInfoKey](
      scalaVersion,
      sbtVersion,
      BuildInfoKey("laminarVersion" -> Dependencies.laminarVersion)
    ),
    // The BuildInfo case class is located in target/scala<version>/src_managed,
    // and with this setting, you'll need to `import com.raquo.buildinfo.BuildInfo`
    // to use it.
    buildInfoPackage := "org.plasmalabs.buildinfo",
    // Because we add BuildInfo to the `shared` project, this will be available
    // on both the client and the server, but you can also make it e.g. server-only.
    homepage := Some(url("https://github.com/PlasmaLaboratories/plasma-cli")),
    licenses := List("MPL2.0" -> url("https://www.mozilla.org/en-US/MPL/2.0/")),
    ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org",
    sonatypeRepository := "https://s01.oss.sonatype.org/service/local",
    developers := List(
      Developer(
        "mundacho",
        "Edmundo Lopez Bobeda",
        "e.lopez@topl.me",
        url("https://github.com/mundacho")
      ),
      Developer(
        "DiademShoukralla",
        "Diadem Shoukralla",
        "d.shoukralla@topl.me",
        url("https://github.com/DiademShoukralla")
      ),
      Developer(
        "scasplte2",
        "James Aman",
        "j.aman@topl.me",
        url("https://github.com/scasplte2")
      )
    )
  )
  .settings(
    libraryDependencies ++= List(
      "io.circe" %%% "circe-core"    % Dependencies.circeVersion,
      "io.circe" %%% "circe-generic" % Dependencies.circeVersion,
      "io.circe" %%% "circe-parser"  % Dependencies.circeVersion
    )
  )
  .jvmSettings(
    libraryDependencies ++= List(
      // This dependency lets us put @JSExportAll and similar Scala.js
      // annotations on data structures shared between JS and JVM.
      // With this library, on the JVM, these annotations compile to
      // no-op, which is exactly what we need.
      "org.scala-js" %% "scalajs-stubs" % "1.1.0"
    )
  )

lazy val gui = project
  .in(file("./gui"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= List(
      "com.raquo" %%% "laminar"  % Dependencies.laminarVersion,
      "com.raquo" %%% "waypoint" % "8.0.1"
    ),
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
      // .withModuleSplitStyle(
      //   ModuleSplitStyle.SmallModulesFor(List("com.raquo.app")))
    },
    // Generated scala.js output will call your main() method to start your app.
    scalaJSUseMainModuleInitializer := true
  )
  .settings(
    // Ignore changes to .less and .css files when watching files with sbt.
    // With the suggested build configuration and usage patterns, these files are
    // not included in the scala.js output, so there is no need for sbt to watch
    // their contents. If sbt was also watching those files, editing them would
    // cause the entire Scala.js app to do a full reload, whereas right now we
    // have Vite watching those files, and it is able to hot-reload them without
    // reloading the entire application – much faster and smoother.
    watchSources := watchSources.value.filterNot { source =>
      source.base.getName.endsWith(".less") || source.base.getName
        .endsWith(".css")
    }
  )
  .settings(noPublish)
  .dependsOn(shared.js)

lazy val root = project
  .in(file("."))
  .settings(
    organization := "org.plasmalabs",
    name := "plasma-cli-umbrella"
  )
  .settings(noPublish)
  .aggregate(gui, cli, shared.jvm) // Note that cliIT is not here, to not triger it test on test task

lazy val cli = project
  .in(file("./cli"))
  .settings(commonSettings)
  .settings(
    organization := "org.plasmalabs",
    name := "plasma-cli",
    fork := true,
    javaOptions += "-Dport=9000", // needed for the cli UI
    resolvers ++= Seq(
      Resolver.defaultLocal,
      "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/",
      "Sonatype Staging" at "https://s01.oss.sonatype.org/content/repositories/staging",
      "Sonatype Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots/",
      "Sonatype Releases" at "https://s01.oss.sonatype.org/content/repositories/releases/",
      "Bintray" at "https://jcenter.bintray.com/",
      ),
    homepage := Some(url("https://github.com/PlasmaLaboratories/plasma-cli")),
    licenses := List("MPL2.0" -> url("https://www.mozilla.org/en-US/MPL/2.0/")),
    ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org",
    sonatypeRepository := "https://s01.oss.sonatype.org/service/local",
    developers := List(
      Developer(
        "mundacho",
        "Edmundo Lopez Bobeda",
        "el@plasma.to",
        url("https://github.com/mundacho")
      ),
      Developer(
        "DiademShoukralla",
        "Diadem Shoukralla",
        "ds@plasma.to",
        url("https://github.com/DiademShoukralla")
      )
    ),
    libraryDependencies ++=
      Dependencies.Cli.sources ++
      Dependencies.Cli.tests
  )
  .settings(
    assembly / mainClass := Some("org.plasmalabs.cli.Main"),
    assembly / assemblyJarName := "plasmacli.jar",

    // Gets rid of "(server / assembly) deduplicate: different file contents found in the following" errors
    // https://stackoverflow.com/questions/54834125/sbt-assembly-deduplicate-module-info-class
    assembly / assemblyMergeStrategy := {
      case x if x.contains("io.netty.versions.properties") =>
        MergeStrategy.discard
      case path if path.endsWith("module-info.class") => MergeStrategy.discard
      case path =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(path)
    }
  )
  .dependsOn(shared.jvm)

lazy val integration = project
  .in(file("./integration"))
  .settings(
    name := "integration",
    commonSettings,
    fork := true,
    javaOptions += "-Dport=9000", // needed for the cli UI
    libraryDependencies ++= Dependencies.Inegration.tests
  ).dependsOn(
    cli
  )

lazy val noPublish = Seq(
  publishLocal / skip := true,
  publish / skip := true
)

lazy val commonSettings = Seq(
  scalaVersion := scalacVersion,
  scalacOptions ++= Seq(
    "-deprecation",
    "-Wunused:imports"
  )
)

val buildClient = taskKey[Unit]("Build client (frontend)")

buildClient := {
  // Generate Scala.js JS output for production
  (gui / Compile / fullLinkJS).value

  // Install JS dependencies from package-lock.json
  val npmCiExitCode = Process("npm ci", cwd = (gui / baseDirectory).value).!
  if (npmCiExitCode > 0) {
    throw new IllegalStateException(s"npm ci failed. See above for reason")
  }

  // Build the frontend with vite
  val buildExitCode =
    Process("npm run build", cwd = (gui / baseDirectory).value).!
  if (buildExitCode > 0) {
    throw new IllegalStateException(
      s"Building frontend failed. See above for reason"
    )
  }

  // Copy vite output into server resources, where it can be accessed by the server,
  // even after the server is packaged in a fat jar.
  IO.copyDirectory(
    source = (gui / baseDirectory).value / "dist",
    target = (cli / baseDirectory).value / "src" / "main" / "resources" / "static"
  )
}

addCommandAlias("checkPR", s"; scalafixAll --check; scalafmtCheckAll; integration/scalafixAll --check; integration/scalafmtCheckAll; cli/test")
addCommandAlias("preparePR", s"; scalafixAll; scalafmtAll; integration/scalafixAll; integration/scalafmtAll; cli/test")
