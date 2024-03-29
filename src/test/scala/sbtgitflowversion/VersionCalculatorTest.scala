package sbtgitflowversion

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sbt.VersionNumber

class VersionCalculatorTest extends AnyFlatSpec with Matchers {

  val prev = VersionNumber("1.2.3")
  val cur = VersionNumber("1.3.0")
  val max = VersionNumber("1.4.0")

  "VersionCalculator" should "support last version policy" in {
    VersionCalculator.lastVersion()(prev, Some(cur), max, Some("foo")) shouldEqual Right(prev)
    VersionCalculator.lastVersion()(prev, None, max, Some("foo")) shouldEqual Right(prev)
    VersionCalculator.lastVersion()(prev, Some(cur), max, None) shouldEqual Right(prev)
    VersionCalculator.lastVersion()(prev, None, max, None) shouldEqual Right(prev)
  }

  it should "support last version with suffix policy" in {
    VersionCalculator.lastVersionWithSuffix("epic")(prev, Some(cur), max, Some("foo")) shouldEqual Right(
      VersionNumber("1.2.3-epic-SNAPSHOT")
    )

    VersionCalculator.lastVersionWithSuffix("epic", isSnapshot = false)(prev, None, max, Some("foo")) shouldEqual Right(
      VersionNumber("1.2.3-epic")
    )

    VersionCalculator.lastVersionWithSuffix("suffix")(prev, Some(cur), max, None) shouldEqual Right(
      VersionNumber("1.2.3-suffix-SNAPSHOT")
    )
    VersionCalculator.lastVersionWithSuffix("test", isSnapshot = false)(prev, None, max, None) shouldEqual Right(
      VersionNumber("1.2.3-test")
    )
  }

  it should "support version with matching" in {
    VersionCalculator.lastVersionWithMatching()(prev, Some(cur), max, Some("foo")) shouldEqual Right(
      VersionNumber("1.2.3-foo-SNAPSHOT")
    )
    VersionCalculator.lastVersionWithMatching()(prev, None, max, Some("foo")) shouldEqual Right(
      VersionNumber("1.2.3-foo-SNAPSHOT")
    )
    VersionCalculator.lastVersionWithMatching()(prev, Some(cur), max, None) shouldEqual Left(
      "Empty matching is not allowed for lastVersionWithMatching policy"
    )
  }

  it should "support matching policy" in {
    VersionCalculator.matching()(prev, Some(cur), max, Some("2.3.4")) shouldEqual Right(VersionNumber("2.3.4-SNAPSHOT"))
    VersionCalculator.matching(isSnapshot = false)(prev, None, max, Some("2.3.4")) shouldEqual Right(
      VersionNumber("2.3.4")
    )
    VersionCalculator.matching()(prev, Some(cur), max, None) shouldEqual Left(
      "Empty matching is not allowed for matching policy"
    )
    VersionCalculator.matching()(prev, None, max, Some("abc")) shouldEqual Left(
      "Empty matching is not allowed for matching policy"
    )
  }

  it should "support current tag policy" in {
    VersionCalculator.currentTag()(prev, Some(cur), max, Some("foo")) shouldEqual Right(cur)
    VersionCalculator.currentTag()(prev, Some(cur), max, None) shouldEqual Right(cur)
    VersionCalculator.currentTag()(prev, None, max, Some("foo")) shouldEqual Left("No tag defined for current version")
  }

  it should "support nextX policies" in {
    VersionCalculator.nextBuild()(prev, Some(cur), max, Some("foo")) shouldEqual Right(VersionNumber("1.2.4-SNAPSHOT"))
    VersionCalculator.nextMinor()(prev, Some(cur), max, Some("foo")) shouldEqual Right(VersionNumber("1.3.0-SNAPSHOT"))
    VersionCalculator.nextMajor(isSnapshot = false)(prev, Some(cur), max, Some("foo")) shouldEqual Right(
      VersionNumber("2.0.0")
    )
  }

  it should "support globalNextX policies" in {
    VersionCalculator.nextGlobalBuild()(prev, Some(cur), max, Some("foo")) shouldEqual Right(
      VersionNumber("1.4.1-SNAPSHOT")
    )
    VersionCalculator.nextGlobalMinor()(prev, Some(cur), max, Some("foo")) shouldEqual Right(
      VersionNumber("1.5.0-SNAPSHOT")
    )
    VersionCalculator.nextGlobalMajor(isSnapshot = false)(prev, Some(cur), max, Some("foo")) shouldEqual Right(
      VersionNumber("2.0.0")
    )
  }

  it should "support fixed policy" in {
    VersionCalculator.fixed(true, 1, 2)(prev, Some(cur), max, Some("x")) shouldEqual Right(
      VersionNumber("1.2-SNAPSHOT")
    )
    VersionCalculator.fixed(false, 3, 4, 5)(prev, None, max, None) shouldEqual Right(VersionNumber("3.4.5"))
  }
}
