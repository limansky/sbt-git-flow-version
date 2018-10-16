package sbtgitflowversion

abstract class VersionPolicy extends ((CurrentRevision, Settings) => Either[String, String])

object VersionPolicy {
  val SNAPSHOT = "SNAPSHOT"

  val lastTag: VersionPolicy = new VersionPolicy {
    override def apply(revision: CurrentRevision, settings: Settings): Either[String, String] = {
      val last = revision.lastTag.getOrElse(settings.initialVersion)
      Right(s"$last-$SNAPSHOT")
    }
  }

  def currentTag: VersionPolicy = new VersionPolicy {
    override def apply(revision: CurrentRevision, settings: Settings): Either[String, String] = {
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

  val nextTag: VersionPolicy = new VersionPolicy {
    override def apply(revision: CurrentRevision, settings: Settings): Either[String, String] = ???
  }

  val unknownVersion: VersionPolicy = new VersionPolicy {
    override def apply(revision: CurrentRevision, settings: Settings): Either[String, String] = {
      Left(s"Don't know how to calculate version for $revision")
    }
  }

  val defaultPolicy: VersionPolicy = new VersionPolicy {
    override def apply(revision: CurrentRevision, settings: Settings): Either[String, String] = {
      revision.branchName match {
        case "master" => currentTag(revision, settings)

        case "develop" => nextTag(revision, settings)

        //    case x if x.startsWith("release/") => extractVersion

        case _ => unknownVersion(revision, settings)
      }
    }
  }

}
