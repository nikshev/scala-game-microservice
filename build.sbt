name := """play-reactive-mongo-db"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

routesGenerator := InjectedRoutesGenerator

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.12",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "org.specs2" %% "specs2-core" % "3.9.5" % "test"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers += Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)

scalacOptions in ThisBuild ++= Seq("-feature", "-language:postfixOps")

fork in run := true