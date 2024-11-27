import sbt._

object Dependencies {


  val sdkVersion = "0.2.2"
  val laminarVersion = "17.1.0"
  val circeVersion = "0.15.0-M1"
  val monocleVersion = "3.3.0"
  val htt4sVersion = "0.23.29"

  val monocleCore = "dev.optics" %% "monocle-core" % monocleVersion
  val monocleMacro = "dev.optics" %% "monocle-macro" % monocleVersion


  val plasmaOrg = "org.plasmalabs"
  val plasmaSdk = plasmaOrg %% "plasma-sdk" % sdkVersion
  val sdkCrypto = plasmaOrg %% "crypto" % sdkVersion
  val sdkServiceKit = plasmaOrg %% "service-kit" % sdkVersion

  val grpcNetty =
    "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion

  val grpcRuntime =
    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion

  val catEffects = "org.typelevel" %% "cats-effect" % "3.3.12"
  val scopt = "com.github.scopt" %% "scopt" % "4.1.0"
  val munit = "org.scalameta" %% "munit" % "1.0.2" % "it,test"
  val fs2Core = "co.fs2" %% "fs2-core" % "3.11.0"
  val fs2IO = "co.fs2" %% "fs2-io" % "3.11.0"
  val logback = "ch.qos.logback" % "logback-classic" % "1.5.12"
  val sqlite = "org.xerial" % "sqlite-jdbc" % "3.47.1.0"
  val fastparse = "com.lihaoyi" %% "fastparse" % "3.1.1"
  val munitCatsEffects ="org.typelevel" %% "munit-cats-effect" % "2.0.0" % "it,test"
  val circeYaml = "io.circe" %% "circe-yaml-v12" % "1.15.0"
  val circeGenericJVM = "io.circe" %% "circe-generic" % circeVersion
  val log4cats = "org.typelevel" %% "log4cats-slf4j" % "2.7.0"

  val http4sEmber = "org.http4s" %% "http4s-ember-server" % htt4sVersion
  val http4sCirce = "org.http4s" %% "http4s-circe" % htt4sVersion
  val http4sDsl = "org.http4s" %% "http4s-dsl" % htt4sVersion

}
