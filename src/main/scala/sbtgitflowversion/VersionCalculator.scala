package sbtgitflowversion

import sbt.VersionNumber

abstract class VersionCalculator(isSnapshot: Boolean) {
  val SNAPSHOT = "SNAPSHOT"

  protected def doCalc(
      previous: VersionNumber,
      current: Option[VersionNumber],
      matching: Option[String]
  ): Either[String, VersionNumber]

  def apply(
      previous: VersionNumber,
      current: Option[VersionNumber],
      matching: Option[String]
  ): Either[String, VersionNumber] = {
    doCalc(previous, current, matching).right.map { r =>
      if (isSnapshot && !r.tags.contains(SNAPSHOT)) {
        VersionNumber(r.numbers, r.tags :+ SNAPSHOT, r.extras)
      } else r
    }
  }
}

object VersionCalculator {
  def lastVersion(isSnapshot: Boolean = false): VersionCalculator =
    new VersionCalculator(isSnapshot) {
      override def doCalc(
          previous: VersionNumber,
          current: Option[VersionNumber],
          matching: Option[String]
      ): Either[String, VersionNumber] = {
        Right(previous)
      }
    }

  def lastVersionWithSuffix(suffix: String, isSnapshot: Boolean = true): VersionCalculator =
    new VersionCalculator(isSnapshot) {
      override def doCalc(
          previous: VersionNumber,
          current: Option[VersionNumber],
          matching: Option[String]
      ): Either[String, VersionNumber] = {
        Right(VersionNumber(previous.numbers, Seq(suffix), Seq.empty))
      }
    }

  def lastVersionWithMatching(isSnapshot: Boolean = true): VersionCalculator =
    new VersionCalculator(isSnapshot) {
      override def doCalc(
          previous: VersionNumber,
          current: Option[VersionNumber],
          matching: Option[String]
      ): Either[String, VersionNumber] = {
        matching
          .map(m => VersionNumber(previous.numbers, Seq(m), Seq.empty))
          .toRight("Empty matching is not allowed for lastVersionWithMatching policy")
      }
    }

  def matching(isSnapshot: Boolean = true): VersionCalculator =
    new VersionCalculator(isSnapshot) {
      override def doCalc(
          previous: VersionNumber,
          current: Option[VersionNumber],
          matching: Option[String]
      ): Either[String, VersionNumber] = {
        matching.flatMap(Version.parse).toRight(s"Empty matching is not allowed for matching policy")
      }
    }

  def currentTag(isSnapshot: Boolean = false): VersionCalculator =
    new VersionCalculator(isSnapshot) {
      override def doCalc(
          previous: VersionNumber,
          current: Option[VersionNumber],
          matching: Option[String]
      ): Either[String, VersionNumber] = {
        current.toRight("No tag defined for current version")
      }
    }

  def nextMajor(isSnapshot: Boolean = true): VersionCalculator = nextN(0, isSnapshot)

  def nextMinor(isSnapshot: Boolean = true): VersionCalculator = nextN(1, isSnapshot)

  def nextBuild(isSnapshot: Boolean = true): VersionCalculator = nextN(2, isSnapshot)

  def nextN(n: Int, isSnapshot: Boolean = true): VersionCalculator =
    new VersionCalculator(isSnapshot) {
      override def doCalc(
          previous: VersionNumber,
          current: Option[VersionNumber],
          matching: Option[String]
      ): Either[String, VersionNumber] = {
        Right(Version.next(n)(previous))
      }
    }

  val unknownVersion: VersionCalculator = new VersionCalculator(false) {
    override def doCalc(
        previous: VersionNumber,
        current: Option[VersionNumber],
        matching: Option[String]
    ): Either[String, VersionNumber] = {
      Left(s"Don't know how to calculate version")
    }
  }
}
