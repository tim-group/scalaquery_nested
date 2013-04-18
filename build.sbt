name := "scalaquery_nested"

version := "1.0.0"

scalaVersion := "2.9.1"

crossScalaVersions := Seq("2.9.0-1", "2.9.1", "2.9.2", "2.10.0")

libraryDependencies ++= Seq(
  // Hard-code compile dependency to last pre-slick release: 0.10.0-M1 on Scala 2.9.1
  "org.scalaquery" % "scalaquery_2.9.1" % "0.10.0-M1" intransitive(),
  // Test-only dependencies
  "org.mockito" % "mockito-core" % "1.9.0" % "test"
)

libraryDependencies <++= scalaVersion(specs2Dependencies(_))

libraryDependencies <++= scalaVersion(scalaCompilerDependency(_))
