name := "scalaquery_nested"

organization := "com.timgroup"

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
      <name>The Apache Software License, Version 2.0</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
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
    </developer>
  </developers>
)

credentials += Credentials(Path.userHome / ".sbt" / "sonatype.credentials")
