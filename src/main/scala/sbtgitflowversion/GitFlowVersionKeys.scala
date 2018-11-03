package sbtgitflowversion

import sbt.settingKey

trait GitFlowVersionKeys {
  lazy val initialVersion = settingKey[String]("Initial version")
  lazy val tagMatcher = settingKey[TagMatcher]("Tag matcher")
  lazy val versionPolicy = settingKey[Seq[(BranchMatcher, VersionCalculator)]]("Current version policy")
}

object GitFlowVersionKeys extends GitFlowVersionKeys
