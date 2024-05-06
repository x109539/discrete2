
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.14"

lazy val root = (project in file("."))
  .settings(
    name := "final_project"
  )

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.6.16"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.6.16"
libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5.13"
libraryDependencies ++= Seq(
  "com.softwaremill.sttp.client3" %% "core" % "3.3.15",
  "com.softwaremill.sttp.client3" %% "circe" % "3.3.15",
  "io.circe" %% "circe-generic" % "0.14.1",
  "io.circe" %% "circe-parser" % "0.14.1"
)
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.2"

libraryDependencies += "org.jfree" % "jfreechart" % "1.5.3"
libraryDependencies += "org.jfree" % "jcommon" % "1.0.24"
libraryDependencies += "org.json4s" %% "json4s-native" % "4.0.3"
libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.10"
libraryDependencies += "org.knowm.xchart" % "xchart" % "3.8.0"

libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.8"
