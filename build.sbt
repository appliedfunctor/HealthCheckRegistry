name := "AsyncIO"

version := "0.1"

scalaVersion := "2.12.5"

scalacOptions += "-Ypartial-unification"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.0.1",
  "org.typelevel" %% "cats-effect" % "1.0.0-RC",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)