package sbtgitflowversion

import sbt.VersionNumber

abstract class VersionCalculator extends ((VersionNumber, Option[VersionNumber], Option[String]) => Either[String, String])

object VersionCalculator {
  val SNAPSHOT = "SNAPSHOT"

  val lastVersion: VersionCalculator = new VersionCalculator {
    override def apply(previous: VersionNumber, current: Option[VersionNumber], matching: Option[String]): Either[String, String] = {
      Right(previous.toString)
    }
  }

  val lastTagWithMatching: VersionCalculator = new VersionCalculator {
    override def apply(previous: VersionNumber, current: Option[VersionNumber], matching: Option[String]): Either[String, String] = {
      matching.map(m => s"$previous-$m-$SNAPSHOT").toRight("Empty matching is not allowed for lastTagWithMatching policy")
    }
  }

  val matching: VersionCalculator = new VersionCalculator {
    override def apply(previous: VersionNumber, current: Option[VersionNumber], matching: Option[String]): Either[String, String] = {
      matching.toRight(s"Empty matching is not allowed for matching policy")
    }
  }

  val currentTag: VersionCalculator = new VersionCalculator {
    override def apply(previous: VersionNumber, current: Option[VersionNumber], matching: Option[String]): Either[String, String] = {
      current.map(_.toString).toRight("No tag defined for current version")
    }
  }

  val nextMajor: VersionCalculator = new VersionCalculator {
    override def apply(previous: VersionNumber, current: Option[VersionNumber], matching: Option[String]): Either[String, String] = {
      Right(Version.nextMajor(previous).toString)
    }
  }

  val nextMinor: VersionCalculator = new VersionCalculator {
    override def apply(previous: VersionNumber, current: Option[VersionNumber], matching: Option[String]): Either[String, String] = {
      Right(Version.nextMinor(previous).toString)
    }
  }

  val nextBuild: VersionCalculator = new VersionCalculator {
    override def apply(previous: VersionNumber, current: Option[VersionNumber], matching: Option[String]): Either[String, String] = {
      Right(Version.nextBuild(previous).toString)
    }
  }

  def nextN(n: Int): VersionCalculator = new VersionCalculator {
    override def apply(previous: VersionNumber, current: Option[VersionNumber], matching: Option[String]): Either[String, String] = {
      Right(Version.next(n)(previous).toString)
    }
  }

  val unknownVersion: VersionCalculator = new VersionCalculator {
    override def apply(previous: VersionNumber, current: Option[VersionNumber], matching: Option[String]): Either[String, String] = {
      Left(s"Don't know how to calculate version")
    }
  }
}
