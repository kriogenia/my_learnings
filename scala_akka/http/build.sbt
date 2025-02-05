ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "http"
  )

lazy val akkaVersion = "2.8.2"
lazy val akkaHttpVersion = "10.5.2"
lazy val jwtVersion = "5.0.0"
lazy val scalaTestVersion = "3.2.15"

libraryDependencies ++= Seq(
  "com.pauldijou" %% "jwt-spray-json" % jwtVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion
)