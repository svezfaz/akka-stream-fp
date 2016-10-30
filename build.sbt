
val akkaV = "2.4.12"
val catsV = "0.8.0"

val commonSettings = Seq(
  organization := "eu.svez",
  version := "0.1-SNAPSHOT",
  crossScalaVersions := Seq("2.11.8"),
  scalaVersion := crossScalaVersions.value.head,
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaV % Test,
    "org.scalatest" %% "scalatest" % "3.0.0" % Test
  )
)

lazy val `akka-stream-fp` = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    description := "Interop toolkit for akka streams and functional programming libraries",
    name := "akka-stream-fp"
  ).
  aggregate(`akka-stream-cats`)

lazy val `akka-stream-cats` = (project in file("./akka-stream-cats")).
  settings(commonSettings: _*).
  settings(
    description := "Interop toolkit for akka streams and Typelevel cats datatypes",
    name := "akka-stream-cats",
    libraryDependencies ++= Seq("org.typelevel" %% "cats" % catsV)
  )