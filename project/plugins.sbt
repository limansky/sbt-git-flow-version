addSbtPlugin("com.github.sbt"                   % "sbt-release"         % "1.4.0")
addSbtPlugin("org.xerial.sbt"                   % "sbt-sonatype"        % "3.11.3")
addSbtPlugin("com.github.sbt"                   % "sbt-pgp"             % "2.3.0")
addSbtPlugin("org.scalameta"                    % "sbt-scalafmt"        % "2.5.2")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
