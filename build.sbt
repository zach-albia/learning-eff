name := "learning-eff"

version := "0"

scalaVersion := Settings.versions.scala

val commonSettings = Seq(
  scalaVersion := Settings.versions.scala,
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % "0.9.0",
    "io.monix" %% "monix" % "2.3.0",
    "org.atnos" %% "eff" % "4.5.0",
    "org.atnos" %% "eff-monix" % "4.5.0",
    "org.scalatest" %% "scalatest" % "3.0.4" % "test"
  ),
  resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases",
  // to write types like Reader[String, ?]
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3"),
  // to get types like Reader[String, ?] (with more than one type parameter) correctly inferred for scala 2.12.x
  scalacOptions += "-Ypartial-unification",
  scalacOptions in Test += "-Yrangepos",
  version := "0"
)

lazy val elementary1 = (project in file("problems/elementary/prob1")).settings(commonSettings)
