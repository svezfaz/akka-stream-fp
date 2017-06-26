
val commonSettings = Seq(
  organization := "eu.svez",
  version := "0.1-SNAPSHOT",
  crossScalaVersions := Seq("2.11.11", "2.12.2"),
  scalaVersion := crossScalaVersions.value.head,
  Dependencies.common
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
    Dependencies.cats
  ).
  dependsOn(`akka-stream-fp-scala`)
