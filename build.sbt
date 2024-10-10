import ReleaseTransformations._

Global / excludeLintKeys += crossSbtVersions

lazy val sbtGitFlowVersion = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-git-flow-version",
    crossScalaVersions := List("2.12.20", "3.3.4"),
    scalacOptions := {
      scalaBinaryVersion.value match {
        case "2.12" => Seq("-deprecation", "-unchecked", "-Xlint", "-feature")
        case _      => Seq("-deprecation", "-unchecked", "-feature")
      }
    },
    addSbtPlugin("com.github.sbt" % "sbt-git" % "2.1.0"),
    libraryDependencies ++= {
      val scalaTestV = "3.2.19"

      Seq(
        "org.scalatest" %% "scalatest-core"                 % scalaTestV        % Test,
        "org.scalatest" %% "scalatest-shouldmatchers"       % scalaTestV        % Test,
        "org.scalatest" %% "scalatest-flatspec"             % scalaTestV        % Test
      )
    },
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.2.8"
        case _      => "2.0.0-M2"
      }
    },
    buildSettings,
    releaseSettings
  )


lazy val buildSettings = Seq(
  organization := "me.limansky",
  description := "An sbt plugin that offers git flow based version",
  licenses := Seq(("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))),
  developers := List(Developer("limansky", "Mike Limansky", "mike.limansky at gmail.com", url("http://www.limansky.me/"))),
  startYear := Some(2018),
  scmInfo := Some(ScmInfo(url("https://github.com/limansky/sbt-git-flow-version"), "scm:git:git@github.com:limansky/sbt-git-flow-version.git")),
  homepage := scmInfo.value map (_.browseUrl),
  publishTo := sonatypePublishToBundle.value
)


lazy val releaseSettings = Seq(
  releaseCrossBuild := true,
  releaseTagName := { (ThisBuild / version).value },
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    releaseStepCommandAndRemaining("^ test"),
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("^ publishSigned"),
    releaseStepCommand("sonatypeBundleRelease"),
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
)
