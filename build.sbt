
val akkaV = "2.4.16"
val catsV = "0.9.0"

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
  aggregate(`akka-stream-fp-scala`, `akka-stream-fp-cats`)

lazy val `akka-stream-fp-scala` = (project in file("./akka-stream-fp-scala")).
  settings(commonSettings: _*).
  settings(
    description := "Interop toolkit for akka streams and Scala standard library datatypes",
    name := "akka-stream-fp-scala"
  )

lazy val `akka-stream-fp-cats` = (project in file("./akka-stream-fp-cats")).
  settings(commonSettings: _*).
  settings(
    description := "Interop toolkit for akka streams and Typelevel cats datatypes",
    name := "akka-stream-fp-cats",
    libraryDependencies ++= Seq("org.typelevel" %% "cats" % catsV)
  ).
  dependsOn(`akka-stream-fp-scala`)
