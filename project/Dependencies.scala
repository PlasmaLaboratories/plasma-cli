import sbt._

object Dependencies {

  val sdkVersion = "0.2.2"
  val laminarVersion = "17.1.0"
  val circeVersion = "0.15.0-M1"
  val monocleVersion = "3.3.0"
  val htt4sVersion = "0.23.29"

  private val monocleCore = "dev.optics"  %% "monocle-core"  % monocleVersion
  private val monocleMacro = "dev.optics" %% "monocle-macro" % monocleVersion

  private val plasmaOrg = "org.plasmalabs"
  private val plasmaSdk = plasmaOrg     %% "plasma-sdk"  % sdkVersion
  private val sdkCrypto = plasmaOrg     %% "crypto"      % sdkVersion
  private val sdkServiceKit = plasmaOrg %% "service-kit" % sdkVersion

  private val grpcNetty =
    "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion

  private val grpcRuntime =
    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion

  private val catEffects = "org.typelevel"       %% "cats-effect"       % "3.3.12"
  private val scopt = "com.github.scopt"         %% "scopt"             % "4.1.0"
  private val munit = "org.scalameta"            %% "munit"             % "1.0.2"
  private val fs2Core = "co.fs2"                 %% "fs2-core"          % "3.11.0"
  private val fs2IO = "co.fs2"                   %% "fs2-io"            % "3.11.0"
  private val logback = "ch.qos.logback"          % "logback-classic"   % "1.5.12"
  private val sqlite = "org.xerial"               % "sqlite-jdbc"       % "3.47.0.0"
  private val fastparse = "com.lihaoyi"          %% "fastparse"         % "3.1.1"
  private val munitCatsEffects = "org.typelevel" %% "munit-cats-effect" % "2.0.0"
  private val circeYaml = "io.circe"             %% "circe-yaml-v12"    % "1.15.0"
  private val circeGenericJVM = "io.circe"       %% "circe-generic"     % circeVersion
  private val log4cats = "org.typelevel"         %% "log4cats-slf4j"    % "2.7.0"

  private val http4sEmber = "org.http4s" %% "http4s-ember-server" % htt4sVersion
  private val http4sCirce = "org.http4s" %% "http4s-circe"        % htt4sVersion
  private val http4sDsl = "org.http4s"   %% "http4s-dsl"          % htt4sVersion

  object Cli {

    val sources: Seq[ModuleID] =
      Seq(
        plasmaSdk,
        sdkCrypto,
        sdkServiceKit,
        scopt,
        fs2Core,
        fs2IO,
        logback,
        grpcNetty,
        grpcRuntime,
        sqlite,
        fastparse,
        circeYaml,
        circeGenericJVM,
        monocleCore,
        monocleMacro,
        http4sEmber,
        http4sCirce,
        http4sDsl,
        log4cats
      )

    val tests: Seq[ModuleID] = Seq(munit, munitCatsEffects).map(_ % Test)
  }

  object Inegration {
    val tests: Seq[ModuleID] = Seq(plasmaSdk, fs2Core, munit, munitCatsEffects).map(_ % Test)
  }

}
