ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.0"

lazy val root = (project in file("."))
  .settings(
    name := "ScalaLab6"
  )
scalacOptions ++= Seq("-language:implicitConversions", "-deprecation")
libraryDependencies ++= Seq(
  "com.storm-enroute" %% "scalameter" % "0.21",
  "com.storm-enroute" %% "scalameter-core" % "0.21",
  "org.scala-lang.modules" %% "scala-parallel-collections" % "0.2.0",
  "com.novocode" % "junit-interface" % "0.11" % Test
)

Test / testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-v", "-s")
