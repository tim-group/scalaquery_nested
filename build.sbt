name := "scalaquery_nested"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.9.1"

crossScalaVersions := Seq("2.9.0-1", "2.9.1", "2.9.2", "2.10.0")

libraryDependencies ++= Seq(
  // Hard-code compile dependency to last pre-slick release: 0.10.0-M1 on Scala 2.9.1
  "org.scalaquery" % "scalaquery_2.9.1" % "0.10.0-M1" intransitive(),
  // Test-only dependencies
  "org.mockito" % "mockito-core" % "1.9.0" % "test"
)

libraryDependencies <++= scalaVersion(specs2Dependencies(_))

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/youdevise/scalaquery_nested</url>
  <licenses>
    <license>
      <name>MIT license</name>
      <url>http://www.opensource.org/licenses/mit-license.php</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:youdevise/scalaquery_nested.git</url>
    <connection>scm:git:git@github.com:youdevise/scalaquery_nested.git</connection>
  </scm>
  <developers>
    <developer>
      <id>msiegel</id>
      <name>Marc Siegel</name>
      <email>marc.siegel@timgroup.com</email>
    </developer>
  </developers>)