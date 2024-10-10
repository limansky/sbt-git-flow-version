package sbtgitflowversion

import com.github.sbt.git.GitPlugin
import com.github.sbt.git.SbtGit.git
import com.github.sbt.git.JGit
import org.eclipse.jgit.lib.{ Constants, Repository }
import org.eclipse.jgit.revwalk.RevWalk
import sbt._
import sbt.Keys._

object GitFlowVersionPlugin extends AutoPlugin {
  override def requires: Plugins = GitPlugin

  override def trigger: PluginTrigger = allRequirements

  object autoImport extends GitFlowVersionKeys {
    val BranchMatcher = sbtgitflowversion.BranchMatcher
    type BranchMatcher = sbtgitflowversion.BranchMatcher

    val TagMatcher = sbtgitflowversion.TagMatcher
    type TagMatcher = sbtgitflowversion.TagMatcher

    val VersionCalculator = sbtgitflowversion.VersionCalculator
    type VersionCalculator = sbtgitflowversion.VersionCalculator
  }

  override def projectSettings: Seq[Def.Setting[_]] = {
    import GitFlowVersionKeys._

    Seq(
      initialVersion := "1.0.0",
      tagMatcher := TagMatcher.raw,
      versionPolicy := defaultPolicy,
      ThisBuild / version := calculateVersion(
        sLog.value,
        JGit(baseDirectory.value),
        versionPolicy.value,
        CurrentRevision(
          git.gitCurrentBranch.value,
          git.gitDescribedVersion.value,
          git.gitCurrentTags.value
        ),
        tagMatcher.value,
        initialVersion.value
      )
    )
  }

  private def calculateVersion(
      logger: Logger,
      jGit: JGit,
      policy: Seq[(BranchMatcher, VersionCalculator)],
      revision: CurrentRevision,
      tagMatcher: TagMatcher,
      initialVersion: String
  ): String = {
    logger.info("Calculating version")
    val globalPolicy = policy.filter(_._2.globalVersion)
    val version = Version.parse(initialVersion).toRight("Invalid initial version").flatMap { initial =>
      val maxVersion = maxGlobalVersion(jGit, globalPolicy, tagMatcher, initial)
      val currentVersions = revision.currentTags.flatMap(tagMatcher(_))
      val maxCurrent = if (currentVersions.isEmpty) None else Some(currentVersions.max(Version.versionOrdering))
      for {
        last <- previousTags(jGit).map(_.flatMap(tagMatcher(_)).lastOption.getOrElse(initial))
        calculated <- applyPolicy(policy, revision, last, maxCurrent, maxVersion)
      } yield calculated
    }

    version match {
      case Right(value) =>
        logger.info(s"Version set to $value")
        value.toString
      case Left(error) => sys.error(error)
    }
  }

  private def maxGlobalVersion(
      jGit: JGit,
      policy: Seq[(BranchMatcher, VersionCalculator)],
      tagMatcher: TagMatcher,
      initial: VersionNumber
  ): Option[VersionNumber] = {
    lazy val availableVersions = jGit.remoteBranches.filter(_.startsWith("origin/")) flatMap { bn =>
      val version = for {
        _ <- applyPolicy(policy, CurrentRevision(bn.substring(7), None, Nil), initial, None, None)
        last <- previousTags(jGit, bn).map(_.flatMap(tagMatcher(_)).lastOption.getOrElse(initial))
        v <- applyPolicy(policy, CurrentRevision(bn.substring(7), None, Nil), last, None, None)
      } yield v

      version.toOption
    }

    if (availableVersions.nonEmpty) Some(availableVersions.max(Version.versionOrdering)) else None
  }

  private def applyPolicy(
      policy: Seq[(BranchMatcher, VersionCalculator)],
      revision: CurrentRevision,
      last: VersionNumber,
      current: Option[VersionNumber],
      maxVersion: Option[VersionNumber]
  ): Either[String, VersionNumber] = {

    val max = List(Some(last), current, maxVersion).flatten.max(Version.versionOrdering)

    policy.iterator
      .map { case (m, p) => m(revision.branchName).map(_ -> p) }
      .find(_.isDefined)
      .flatten
      .map { case (m, p) => p(current getOrElse last, current, max, m.extraction) }
      .getOrElse(Left(s"No applicable policy for branch ${revision.branchName}"))
  }

  private def previousTags(jGit: JGit, ref: String = Constants.HEAD): Either[String, Seq[String]] = {
    import scala.collection.JavaConverters._

    Option(jGit.repo.resolve(ref))
      .toRight("No HEAD commit found. Possible there is no git repository.")
      .map { head =>
        val revWalk = new RevWalk(jGit.repo)
        val headCommit = revWalk.parseCommit(head)
        val allTags = jGit.porcelain.tagList().call().asScala.toList

        allTags
          .map(ref => (Repository.shortenRefName(ref.getName), revWalk.parseCommit(ref.getObjectId)))
          .filter(x => revWalk.isMergedInto(x._2, headCommit) && x._2 != headCommit)
          .sortBy(_._2.getCommitTime)
          .map(_._1)
      }

  }

  private val defaultPolicy = {
    import BranchMatcher._
    import VersionCalculator._

    Seq(
      exact("master") -> currentTag(),
      exact("develop") -> nextGlobalMinor(),
      prefix("release/") -> matching(),
      prefixes("feature/", "bugfix/", "hotfix/") -> lastVersionWithMatching(),
      any -> unknownVersion
    )
  }
}
