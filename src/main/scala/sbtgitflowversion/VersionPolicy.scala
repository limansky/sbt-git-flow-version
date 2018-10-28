package sbtgitflowversion

abstract class VersionPolicy extends ((CurrentRevision, Settings, Option[String]) => Either[String, String])

object VersionPolicy {
  val SNAPSHOT = "SNAPSHOT"

  val lastTag: VersionPolicy = new VersionPolicy {
    override def apply(revision: CurrentRevision, settings: Settings, matching: Option[String]): Either[String, String] = {
      val last = revision.lastTag.getOrElse(settings.initialVersion)
      Right(s"$last-$SNAPSHOT")
    }
  }

  val lastTagWithMatching: VersionPolicy = new VersionPolicy {
    override def apply(revision: CurrentRevision, settings: Settings, matching: Option[String]): Either[String, String] = {
      val last = revision.lastTag.getOrElse(settings.initialVersion)
      matching.map(m => s"$last-$m-$SNAPSHOT").toRight(s"Empty matching is not allowed for $revision")
    }
  }

  val matching: VersionPolicy = new VersionPolicy {
    override def apply(revision: CurrentRevision, settings: Settings, matching: Option[String]): Either[String, String] = {
      matching.toRight(s"Empty matching is not allowed for $revision")
    }
  }

  val currentTag: VersionPolicy = new VersionPolicy {
    override def apply(revision: CurrentRevision, settings: Settings, matching: Option[String]): Either[String, String] = {
      val c = revision.currentTags.filter(settings.tagFilter)
      if (c.isEmpty) {
        Left("No tag defined for current version")
      } else if (c.size > 1) {
        val allTags = c.mkString(", ")
        Left(s"Too many tags: $allTags")
      } else {
        Right(c.head)
      }
    }
  }

  val nextMajor: VersionPolicy = new VersionPolicy {
    override def apply(revision: CurrentRevision, settings: Settings, matching: Option[String]): Either[String, String] = ???
  }

  val nextMinor: VersionPolicy = new VersionPolicy {
    override def apply(revision: CurrentRevision, settings: Settings, matching: Option[String]): Either[String, String] = ???
  }

  val nextBuild: VersionPolicy = new VersionPolicy {
    override def apply(revision: CurrentRevision, settings: Settings, matching: Option[String]): Either[String, String] = ???
  }

  def nextN(n: Int): VersionPolicy = new VersionPolicy {
    override def apply(revision: CurrentRevision, settings: Settings, matching: Option[String]): Either[String, String] = ???
  }

  val unknownVersion: VersionPolicy = new VersionPolicy {
    override def apply(revision: CurrentRevision, settings: Settings, matching: Option[String]): Either[String, String] = {
      Left(s"Don't know how to calculate version for $revision")
    }
  }
}
