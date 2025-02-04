ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.5"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.5.7",
  "co.fs2" %% "fs2-core" % "3.11.0",
  "org.apache.kafka" % "kafka-clients" % "3.9.0",
  "com.github.fd4s" %% "fs2-kafka" % "3.6.0",
//  "com.github.fd4s" %% "fs2-kafka-vulcan" % "3.6.0",
  "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % "8.11.5",
  "com.sksamuel.elastic4s" %% "elastic4s-effect-cats" % "8.11.5",
  "com.sksamuel.elastic4s" %% "elastic4s-json-circe" % "8.11.5",

  "io.circe" %% "circe-core" % "0.14.10",
  "org.typelevel" %% "log4cats-slf4j" % "2.7.0",
  "ch.qos.logback" % "logback-classic" % "1.5.16"
)

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.14.3"

resolvers += "Confluent Repository" at "https://packages.confluent.io/maven/"

lazy val root = (project in file("."))
  .settings(
    name := "tracker"
  )
