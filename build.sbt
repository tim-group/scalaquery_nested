name := "scalaquery_nested"

organization := "com.timgroup"

// Change to non-SNAPSHOT version to publish a release
//version := "1.0.0"
version := "1.0.1-SNAPSHOT"

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

publishTo <<= version { v: String =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
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
      <id>ms-tg</id>
      <name>Marc Siegel</name>
      <email>marc.siegel@timgroup.com</email>
    </developer>
  </developers>
)

// NOTE (2013-04-22, Marc): To publish to Sonatype:
//
// 1. Install sbt-extras to handle multiple versions of sbt gracefully
//      https://github.com/paulp/sbt-extras
//
// 2. Generate and publish your GPG key
//      https://docs.sonatype.org/display/Repository/How+To+Generate+PGP+Signatures+With+Maven
//
// 3. Setup sbt-pgp plugin
//      ~/.sbt/0.12.3/plugins/gpg.sbt
//          addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8")
//
// 4. Setup local Sonatype credentials
//      ~/.sbt/0.12.3/plugins/sonatype.sbt:
//          credentials += Credentials("Sonatype Nexus Repository Manager",
//                                     "oss.sonatype.org",
//                                     USERNAME,
//                                     PASSWORD)
//
// 5. Bump version
//       For snapshot versions: 1.0.0-SNAPSHOT
//       For releases: 1.0.0
//
// 6. Publish
//       sbt-extras.sh clean publish-signed
