package sbtgitflowversion

import sbt.VersionNumber

abstract class VersionCalculator(isSnapshot: Boolean, val globalVersion: Boolean) {
  protected def doCalc(
      previous: VersionNumber,
      current: Option[VersionNumber],
      max: VersionNumber,
      matching: Option[String]
  ): Either[String, VersionNumber]

  def apply(
      previous: VersionNumber,
      current: Option[VersionNumber],
      max: VersionNumber,
      matching: Option[String]
  ): Either[String, VersionNumber] = {
    doCalc(previous, current, max, matching).map { r =>
      if (isSnapshot && !r.tags.contains(VersionCalculator.SNAPSHOT)) {
        VersionNumber(r.numbers, r.tags :+ VersionCalculator.SNAPSHOT, r.extras)
      } else r
    }
  }
}

object VersionCalculator {
  val SNAPSHOT = "SNAPSHOT"

  def lastVersion(isSnapshot: Boolean = false, globalVersion: Boolean = false): VersionCalculator =
    new VersionCalculator(isSnapshot, globalVersion) {
      override def doCalc(
          previous: VersionNumber,
          current: Option[VersionNumber],
          max: VersionNumber,
          matching: Option[String]
      ): Either[String, VersionNumber] = {
        Right(previous)
      }
    }

  def lastVersionWithSuffix(
      suffix: String,
      isSnapshot: Boolean = true,
      globalVersion: Boolean = false
  ): VersionCalculator =
    new VersionCalculator(isSnapshot, globalVersion) {
      override def doCalc(
          previous: VersionNumber,
          current: Option[VersionNumber],
          max: VersionNumber,
          matching: Option[String]
      ): Either[String, VersionNumber] = {
        Right(VersionNumber(previous.numbers, Seq(suffix), Seq.empty))
      }
    }

  def lastVersionWithMatching(isSnapshot: Boolean = true, globalVersion: Boolean = false): VersionCalculator =
    new VersionCalculator(isSnapshot, globalVersion) {
      override def doCalc(
          previous: VersionNumber,
          current: Option[VersionNumber],
          max: VersionNumber,
          matching: Option[String]
      ): Either[String, VersionNumber] = {
        matching
          .map(m => VersionNumber(previous.numbers, Seq(m), Seq.empty))
          .toRight("Empty matching is not allowed for lastVersionWithMatching policy")
      }
    }

  def matching(isSnapshot: Boolean = true, globalVersion: Boolean = true): VersionCalculator =
    new VersionCalculator(isSnapshot, globalVersion) {
      override def doCalc(
          previous: VersionNumber,
          current: Option[VersionNumber],
          max: VersionNumber,
          matching: Option[String]
      ): Either[String, VersionNumber] = {
        matching.flatMap(Version.parse).toRight(s"Empty matching is not allowed for matching policy")
      }
    }

  def currentTag(isSnapshot: Boolean = false, globalVersion: Boolean = false): VersionCalculator =
    new VersionCalculator(isSnapshot, globalVersion) {
      override def doCalc(
          previous: VersionNumber,
          current: Option[VersionNumber],
          max: VersionNumber,
          matching: Option[String]
      ): Either[String, VersionNumber] = {
        current.toRight("No tag defined for current version")
      }
    }

  def nextMajor(isSnapshot: Boolean = true, globalVersion: Boolean = false): VersionCalculator =
    nextN(0, isSnapshot, globalVersion)

  def nextMinor(isSnapshot: Boolean = true, globalVersion: Boolean = false): VersionCalculator =
    nextN(1, isSnapshot, globalVersion)

  def nextBuild(isSnapshot: Boolean = true, globalVersion: Boolean = false): VersionCalculator =
    nextN(2, isSnapshot, globalVersion)

  def nextN(n: Int, isSnapshot: Boolean = true, globalVersion: Boolean = false): VersionCalculator =
    new VersionCalculator(isSnapshot, globalVersion) {
      override def doCalc(
          previous: VersionNumber,
          current: Option[VersionNumber],
          max: VersionNumber,
          matching: Option[String]
      ): Either[String, VersionNumber] = {
        Right(Version.next(n)(previous))
      }
    }

  def nextGlobalMajor(isSnapshot: Boolean = true, globalVersion: Boolean = false): VersionCalculator =
    nextGlobalN(0, isSnapshot, globalVersion)

  def nextGlobalMinor(isSnapshot: Boolean = true, globalVersion: Boolean = false): VersionCalculator =
    nextGlobalN(1, isSnapshot, globalVersion)

  def nextGlobalBuild(isSnapshot: Boolean = true, globalVersion: Boolean = false): VersionCalculator =
    nextGlobalN(2, isSnapshot, globalVersion)

  def nextGlobalN(n: Int, isSnapshot: Boolean = true, globalVersion: Boolean = false): VersionCalculator =
    new VersionCalculator(isSnapshot, globalVersion) {
      override def doCalc(
          previous: VersionNumber,
          current: Option[VersionNumber],
          max: VersionNumber,
          matching: Option[String]
      ): Either[String, VersionNumber] = {
        Right(Version.next(n)(max))
      }
    }

  def fixed(isSnapshot: Boolean, parts: Long*): VersionCalculator = new VersionCalculator(isSnapshot, false) {
    override protected def doCalc(
        previous: VersionNumber,
        current: Option[VersionNumber],
        max: VersionNumber,
        matching: Option[String]
    ): Either[String, VersionNumber] = Right(VersionNumber(parts.toSeq, Seq.empty, Seq.empty))
  }

  val unknownVersion: VersionCalculator = new VersionCalculator(false, false) {
    override def doCalc(
        previous: VersionNumber,
        current: Option[VersionNumber],
        max: VersionNumber,
        matching: Option[String]
    ): Either[String, VersionNumber] = {
      Left(s"Don't know how to calculate version")
    }
  }
}
