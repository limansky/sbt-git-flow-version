package sbtgitflowversion

import com.typesafe.sbt.GitPlugin
import com.typesafe.sbt.SbtGit.git
import com.typesafe.sbt.git.JGit
import org.eclipse.jgit.lib.{Constants, Repository}
import org.eclipse.jgit.revwalk.RevWalk
import sbt._
import sbt.Keys._

object GitFlowVersionPlugin extends AutoPlugin {
  override def requires: Plugins = GitPlugin

  override def trigger: PluginTrigger = allRequirements

  object GitFlowVersionKeys {
    val initialVersion = settingKey[String]("Initial version")
    val tagMatcher = settingKey[TagMatcher]("Tag matcher")
    val policy = settingKey[Seq[(BranchMatcher, VersionPolicy)]]("Current version policy")
  }

  override def projectSettings: Seq[Def.Setting[_]] = {
    import GitFlowVersionKeys._

    Seq(
      initialVersion := "1.0.0",
      tagMatcher := TagMatcher.raw,
      policy := defaultPolicy,
      version in ThisBuild := calculateVersion(
        sLog.value,
        JGit(baseDirectory.value),
        policy.value,
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
    policy: Seq[(BranchMatcher, VersionPolicy)],
    revision: CurrentRevision,
    tagMatcher: TagMatcher,
    initialVersion: String
  ): String = {
    logger.info("Calculating version")

    val currentVersions = revision.currentTags.flatMap(tagMatcher(_))
    val maxCurrent = if (currentVersions.isEmpty) None else Some(currentVersions.max(Version.versionOrdering))

    val version = for {
      initial <- Version.parse(initialVersion).toRight("Invalid initial version").right
      last <- previousTags(jGit).right.map(_.flatMap(tagMatcher(_)).headOption.getOrElse(initial)).right
      calculated <- applyPolicy(policy, revision, last, maxCurrent).right
    } yield calculated

    version match {
      case Right(value) => value
      case Left(error) => sys.error(error)
    }
  }

  private def applyPolicy(
    policy: Seq[(BranchMatcher, VersionPolicy)],
    revision: CurrentRevision,
    last: VersionNumber,
    current: Option[VersionNumber]
  ): Either[String, String] = {
    policy.iterator
      .map { case (m, p) => m(revision.branchName).map(_ -> p) }
      .find(_.isDefined).flatten
      .map { case (m, p) => p(last, current, m.extraction) }
      .getOrElse(Left(s"No applicable policy for branch ${revision.branchName}"))
  }

  def previousTags(jGit: JGit): Either[String, Seq[String]] = {
    import scala.collection.JavaConverters._

    for {
      head <- Option(jGit.repo.resolve(Constants.HEAD)).toRight("No HEAD commit found. Possible there is no git repository.").right
    } yield {
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
