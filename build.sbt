name := "gattakka"

version := "1.0"

scalaVersion := "2.12.2"

lazy val akkaVersion = "2.5.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion
)
