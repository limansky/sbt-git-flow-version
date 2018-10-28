package sbtgitflowversion

import com.typesafe.sbt.GitPlugin
import com.typesafe.sbt.SbtGit.git
import sbt._
import sbt.Keys._

object GitFlowVersionPlugin extends AutoPlugin {
  override def requires: Plugins = GitPlugin

  override def trigger: PluginTrigger = allRequirements

  object GitFlowVersionKeys {
    val settings = settingKey[Settings]("All of the sbt-git-flow-version settings")
    val policy = settingKey[Seq[(BranchMatcher, VersionPolicy)]]("Current version policy")
  }

  override def projectSettings: Seq[Def.Setting[_]] = {
    import GitFlowVersionKeys._

    Seq(
      settings := Settings(
        initialVersion = "1.0.0",
        tagPrefix = "",
        tagSuffix = ""
      ),
      policy := defaultPolicy,
      version in ThisBuild := calculateVersion(
        sLog.value,
        policy.value,
        CurrentRevision(
          git.gitCurrentBranch.value,
          git.gitDescribedVersion.value,
          git.gitCurrentTags.value
        ),
        settings.value
      )
    )
  }

  private def calculateVersion(logger: Logger, policy: Seq[(BranchMatcher, VersionPolicy)], revision: CurrentRevision, settings: Settings): String = {
    logger.info("Calculating version")
    policy.iterator
      .map { case (m, p) => m(revision.branchName).map(_ -> p) }
      .find(_.isDefined).flatten
      .map { case (m, p) => p(revision, settings, m.extraction) }
      .getOrElse(Left(s"No applicable policy for branch ${revision.branchName}")) match {
      case Right(value) => value
      case Left(error) => sys.error(error)
    }
  }

  private val defaultPolicy = {
    import BranchMatcher._
    import VersionPolicy._

    Seq(
      exact("master") -> currentTag,
      exact("develop") -> nextMinor,
      prefix("release/") -> matching,
      prefixes("feature/", "bugfix/", "hotfix/") -> lastTagWithMatching,
      any -> unknownVersion
    )
  }
}
