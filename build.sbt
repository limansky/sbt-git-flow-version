organization := "me.limansky"

name := "sbt-git-flow-version"

licenses := Seq(("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")))
description := "An sbt plugin that offers git flow based version"
developers := List(Developer("limansky", "Mike Limansky", "mike.limansky gmail com", url("http://www.limansky.me/")))
startYear := Some(2018)
homepage := scmInfo.value map (_.browseUrl)
scmInfo := Some(ScmInfo(url("https://github.com/limansky/sbt-git-flow-version"), "scm:git:git@github.com:limansky/sbt-git-flow-version.git"))

crossSbtVersions := List("0.13.17", "1.2.4")

enablePlugins(SbtPlugin)

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test