
lazy val root = (project in file("."))
  .settings(
    organization := "eu.svez",
    name := "akka-stream-fp",
    version := "0.1-SNAPSHOT",
    description := "Akka Streams helper toolkit to build complex, referentially transparent streaming applications",
    crossScalaVersions := Seq("2.12.4", "2.11.11"),
    scalaVersion := crossScalaVersions.value.head,
    libraryDependencies ++= Dependencies.all,
    scalacOptions in Compile ++= Seq(
      "-encoding", "UTF-8",
      "-target:jvm-1.8",
      "-feature",
      "-deprecation",
      "-unchecked",
      "-Xlint",
      "-Xfuture",
      "-Ywarn-dead-code",
      "-Ywarn-unused-import",
      "-Ywarn-unused",
      "-Ywarn-nullary-unit"
    )
  )

