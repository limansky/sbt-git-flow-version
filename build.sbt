import ReleaseTransformations._

lazy val sbtGitFlowVersion = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-git-flow-version",
    crossSbtVersions := List("0.13.18", "1.3.2"),
    scalacOptions := Seq("-deprecation", "-unchecked", "-Xlint", "-feature"),
    addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0"),
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % Test,
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
  publishTo := sonatypePublishTo.value
)


lazy val releaseSettings = Seq(
  releaseCrossBuild := true,
  releaseTagName := { (version in ThisBuild).value },
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
    setNextVersion,
    commitNextVersion,
    releaseStepCommand("sonatypeReleaseAll"),
    pushChanges
  )
)
