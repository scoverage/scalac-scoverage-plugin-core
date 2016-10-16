import sbt._
import sbt.Keys._
import sbtrelease.ReleasePlugin.autoImport._
import com.typesafe.sbt.pgp.PgpKeys


val Org = "org.scoverage"
val MockitoVersion = "1.10.19"
val JUnitInterfaceVersion = "0.9"
val JUnitVersion = "4.11"

lazy val fullCrossSettings = Seq(
  crossVersion := CrossVersion.full // because compiler api is not binary compatible
) ++ allCrossSettings

lazy val binaryCrossSettings = Seq(
  crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.0-RC2")
)

lazy val allCrossSettings = Seq(
  crossScalaVersions := Seq(
    "2.10.6",
    "2.11.8",
    "2.12.0-M3",
    "2.12.0-M4",
    "2.12.0-M5",
    "2.12.0-RC1-ceaf419",
    "2.12.0-RC1",
    "2.12.0-RC1-1e81a09",
    "2.12.0-RC2")
)

val appSettings = Seq(
  organization := Org,
  scalaVersion := "2.11.8",
  fork in Test := false,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  parallelExecution in Test := false,
  scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8"),
  javacOptions := {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, scalaMajor)) if scalaMajor < 12 => Seq("-source", "1.7", "-target", "1.7")
      case _ => Seq()
    }
  },
  libraryDependencies +=
    "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",

  concurrentRestrictions in Global += Tags.limit(Tags.Test, 1),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"

    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  pomExtra := {
    <url>https://github.com/scoverage/scalac-scoverage-plugin-core</url>
      <licenses>
        <license>
          <name>Apache 2</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:scoverage/scalac-scoverage-plugin-core.git</url>
        <connection>scm:git@github.com:scoverage/scalac-scoverage-plugin-core.git</connection>
      </scm>
      <developers>
        <developer>
          <id>sksamuel</id>
          <name>Stephen Samuel</name>
          <url>http://github.com/sksamuel</url>
        </developer>
      </developers>
  },
  pomIncludeRepository := {
    _ => false
  }
) ++ Seq(
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value
)

lazy val noPublishSettings = Seq(
  publishArtifact := false,
  // The above is enough for Maven repos but it doesn't prevent publishing of ivy.xml files
  publish := {},
  publishLocal := {}
)

lazy val junitSettings = Seq(
  testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-v"),
  libraryDependencies ++= Seq(
    "com.novocode" % "junit-interface" % JUnitInterfaceVersion % "test",
    "junit" % "junit" % JUnitVersion % "test"
  )
)

lazy val root = Project("scalac-scoverage", file("."))
  .settings(name := "scalac-scoverage")
  .settings(appSettings: _*)
  .settings(allCrossSettings)
  .settings(noPublishSettings)
  .aggregate(plugin, runtimeJava, runtimeScala, pluginTests)

lazy val runtimeJava = Project("scalac-scoverage-runtime-java", file("scalac-scoverage-runtime-java"))
  .settings(name := "scalac-scoverage-runtime-java")
  .settings(appSettings: _*)
  .settings(binaryCrossSettings)
  .settings(junitSettings)
  .dependsOn(pluginTests % "test->compile")

lazy val runtimeScala = Project("scalac-scoverage-runtime-scala", file("scalac-scoverage-runtime-scala"))
  .settings(name := "scalac-scoverage-runtime-scala")
  .settings(appSettings: _*)
  .settings(binaryCrossSettings)
  .settings(junitSettings)
  .dependsOn(pluginTests % "test->compile")

lazy val plugin = Project("scalac-scoverage-plugin", file("scalac-scoverage-plugin"))
  .settings(name := "scalac-scoverage-plugin")
  .settings(appSettings: _*)
  .settings(fullCrossSettings)

lazy val pluginTests = Project("scalac-scoverage-plugin-tests", file("scalac-scoverage-plugin-tests"))
  .dependsOn(plugin)
  .settings(name := "scalac-scoverage-plugin-tests")
  .settings(appSettings: _*)
  .settings(binaryCrossSettings)
  .settings(libraryDependencies ++= Seq(
    "org.mockito" % "mockito-all" % MockitoVersion,
    "com.novocode" % "junit-interface" % JUnitInterfaceVersion
  ))
