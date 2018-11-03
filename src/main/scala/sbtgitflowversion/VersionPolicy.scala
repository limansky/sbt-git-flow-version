package sbtgitflowversion

import sbt.VersionNumber

abstract class VersionPolicy extends ((VersionNumber, Option[VersionNumber], Option[String]) => Either[String, String])

object VersionPolicy {
  val SNAPSHOT = "SNAPSHOT"

  val lastVersion: VersionPolicy = new VersionPolicy {
    override def apply(previous: VersionNumber, current: Option[VersionNumber], matching: Option[String]): Either[String, String] = {
      Right(previous.toString)
    }
  }

  val lastTagWithMatching: VersionPolicy = new VersionPolicy {
    override def apply(previous: VersionNumber, current: Option[VersionNumber], matching: Option[String]): Either[String, String] = {
      matching.map(m => s"$previous-$m-$SNAPSHOT").toRight("Empty matching is not allowed for lastTagWithMatching policy")
    }
  }

  val matching: VersionPolicy = new VersionPolicy {
    override def apply(previous: VersionNumber, current: Option[VersionNumber], matching: Option[String]): Either[String, String] = {
      matching.toRight(s"Empty matching is not allowed for matching policy")
    }
  }

  val currentTag: VersionPolicy = new VersionPolicy {
    override def apply(previous: VersionNumber, current: Option[VersionNumber], matching: Option[String]): Either[String, String] = {
      current.map(_.toString).toRight("No tag defined for current version")
    }
  }

  val nextMajor: VersionPolicy = new VersionPolicy {
    override def apply(previous: VersionNumber, current: Option[VersionNumber], matching: Option[String]): Either[String, String] = {
      Right(Version.nextMajor(previous).toString)
    }
  }

  val nextMinor: VersionPolicy = new VersionPolicy {
    override def apply(previous: VersionNumber, current: Option[VersionNumber], matching: Option[String]): Either[String, String] = {
      Right(Version.nextMinor(previous).toString)
    }
  }

  val nextBuild: VersionPolicy = new VersionPolicy {
    override def apply(previous: VersionNumber, current: Option[VersionNumber], matching: Option[String]): Either[String, String] = {
      Right(Version.nextBuild(previous).toString)
    }
  }

  def nextN(n: Int): VersionPolicy = new VersionPolicy {
    override def apply(previous: VersionNumber, current: Option[VersionNumber], matching: Option[String]): Either[String, String] = {
      Right(Version.next(n)(previous).toString)
    }
  }

  val unknownVersion: VersionPolicy = new VersionPolicy {
    override def apply(previous: VersionNumber, current: Option[VersionNumber], matching: Option[String]): Either[String, String] = {
      Left(s"Don't know how to calculate version")
    }
  }
}
