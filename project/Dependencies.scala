import sbt._, Keys._

object Dependencies {

  val akkaVersion = "2.5.3"
  val catsVersion = "0.9.0"

  val common = libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-stream"         % akkaVersion,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
    "org.scalatest"     %% "scalatest"           % "3.0.3" % Test
  )

  val cats = libraryDependencies ++= Seq(
    "org.typelevel"     %% "cats-core"           % catsVersion
  )
}