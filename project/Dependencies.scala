import sbt._

object Dependencies {

  val akkaVersion = "2.5.3"
  val catsVersion = "0.9.0"

  val all = Seq(
    "org.typelevel"     %% "cats-core"           % catsVersion,
    "com.typesafe.akka" %% "akka-stream"         % akkaVersion,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
    "org.scalatest"     %% "scalatest"           % "3.0.3" % Test
  )
}