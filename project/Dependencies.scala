import sbt._

object Dependencies {

  val akkaVersion      = "2.5.9"
  val catsVersion      = "1.0.1"
  val scalatestVersion = "3.0.4"

  val core = Seq(
    "org.typelevel"     %% "cats-core"           % catsVersion,
    "com.typesafe.akka" %% "akka-stream"         % akkaVersion,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion      % Test,
    "org.scalatest"     %% "scalatest"           % scalatestVersion % Test
  )

  val examples = Seq(
    "com.typesafe.akka" %% "akka-stream-kafka"   % "0.19"
  )
}