addSbtPlugin("com.github.sbt" % "sbt-release"  % "1.5.0")
addSbtPlugin("com.github.sbt" % "sbt-pgp"      % "2.3.1")
addSbtPlugin("org.scalameta"  % "sbt-scalafmt" % "2.6.1")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
