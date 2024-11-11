import sbt._

object Dependencies {

  lazy val plasmaOrg = "org.plasmalabs"
  lazy val sdkVersion = "0.1.0"
  val plasmaSdk = plasmaOrg %% "plasma-sdk" % sdkVersion
  val circeVersion = "0.15.0-M1"

  val monocleCore = "dev.optics" %% "monocle-core" % "3.3.0"

  val monocleMacro = "dev.optics" %% "monocle-macro" % "3.3.0"

  val laminarVersion = "17.1.0"

  val sdkCrypto = plasmaOrg %% "crypto" % sdkVersion
  val sdkServiceKit = plasmaOrg %% "service-kit" % sdkVersion

  val grpcNetty =
    "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion

  val grpcRuntime =
    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion

  lazy val catEffects = "org.typelevel" %% "cats-effect" % "3.3.12"
  lazy val scopt = "com.github.scopt" %% "scopt" % "4.1.0"
  lazy val munit = "org.scalameta" %% "munit" % "1.0.2" % "it,test"
  lazy val fs2Core = "co.fs2" %% "fs2-core" % "3.11.0"
  lazy val fs2IO = "co.fs2" %% "fs2-io" % "3.11.0"
  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.5.12"
  lazy val sqlite = "org.xerial" % "sqlite-jdbc" % "3.45.3.0"
  lazy val fastparse = "com.lihaoyi" %% "fastparse" % "3.0.2"
  lazy val munitCatsEffects =
    "org.typelevel" %% "munit-cats-effect" % "2.0.0" % "it,test"
  lazy val circeYaml = "io.circe" %% "circe-yaml-v12" % "1.15.0"
  lazy val circeGenericJVM = "io.circe" %% "circe-generic" % circeVersion
  lazy val log4cats = "org.typelevel" %% "log4cats-slf4j" % "2.7.0"

  lazy val http4sEmber = "org.http4s" %% "http4s-ember-server" % "0.23.29"
  lazy val http4sCirce = "org.http4s" %% "http4s-circe" % "0.23.29"
  lazy val http4sDsl = "org.http4s" %% "http4s-dsl" % "0.23.29"

}
