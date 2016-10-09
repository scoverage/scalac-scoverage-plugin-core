import sbt._
import sbt.Keys._
import sbtrelease.ReleasePlugin.autoImport._
import com.typesafe.sbt.pgp.PgpKeys


val Org = "org.scoverage"
//val MockitoVersion = "1.10.19"
val JUnitVersion = "0.9"

val appSettings = Seq(
    organization := Org,
    crossVersion := CrossVersion.full, // because compiler api is not binary compatible
    scalaVersion := "2.11.8",
    crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.0-M3", "2.12.0-M4","2.12.0-M5","2.12.0-RC1", "2.12.0-RC1-ceaf419"),
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

lazy val root = Project("scalac-scoverage", file("."))
    .settings(name := "scalac-scoverage")
    .settings(appSettings: _*)
    .settings(publishArtifact := false)
    .aggregate(plugin, runtimeJava, runtimeScala)


lazy val runtimeJava = Project("scalac-scoverage-runtime-java", file("scalac-scoverage-runtime-java"))
    .settings(name := "scalac-scoverage-runtime-java")
    .settings(appSettings: _*)

lazy val runtimeScala = Project("scalac-scoverage-runtime-scala", file("scalac-scoverage-runtime-scala"))
    .settings(name := "scalac-scoverage-runtime-scala")
    .settings(appSettings: _*)

lazy val plugin = Project("scalac-scoverage-plugin", file("scalac-scoverage-plugin"))
    //.dependsOn(`scalac-scoverage-runtime-java` % "test")
    .settings(name := "scalac-scoverage-plugin")
    .settings(appSettings: _*)
    .settings(libraryDependencies ++= Seq(
  //  "org.mockito" % "mockito-all" % MockitoVersion % "test",
   // "org.scalatest" %% "scalatest" % ScalatestVersion % "test",
    "com.novocode" % "junit-interface" % "0.9" % "test",
    "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided" excludeAll(ExclusionRule(organization="org.scala-lang.modules")),
    "org.joda" % "joda-convert" % "1.6" % "test",
    "joda-time" % "joda-time" % "2.3" % "test"
  ))
