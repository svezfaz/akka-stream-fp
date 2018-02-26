
val commonSettings = Seq(
    organization := "eu.svez",
    description := "Akka Streams helper toolkit to build complex, referentially transparent streaming applications",
    crossScalaVersions := Seq("2.12.4", "2.11.11"),
    scalaVersion := crossScalaVersions.value.head,
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

lazy val root = project.in(file("."))
    .settings(commonSettings)
    .settings(publishArtifact := false)
    .aggregate(core, examples)

lazy val core = project.in(file("core"))
  .settings(commonSettings)
  .settings(Seq(
    name := "akka-stream-fp",
    libraryDependencies ++= Dependencies.core,
  ))

lazy val examples = project.in(file("examples"))
  .dependsOn(core)
  .settings(commonSettings)
  .settings(Seq(
    name := "akka-stream-fp-examples",
    publishArtifact := false,
    libraryDependencies ++= Dependencies.examples,
    scalacOptions in Compile ~= { _ filterNot { o ⇒ o == "-Xfatal-warnings" } }
  ))