name := """comcast_PingRen"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.google.code.gson" % "gson" % "2.3",	
  javaJdbc,
  cache,
  javaWs
)
