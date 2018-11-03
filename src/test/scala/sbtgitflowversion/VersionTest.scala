package sbtgitflowversion

import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers, OptionValues}
import sbt.VersionNumber

class VersionTest extends FlatSpec with Matchers with OptionValues with TableDrivenPropertyChecks {

  "Version" should "increment major version" in {
    val data = Table(
      ("old", "new"),
      (VersionNumber("1"), VersionNumber("2")),
      (VersionNumber("3.1"), VersionNumber("4.0")),
      (VersionNumber("1.0.3"), VersionNumber("2.0.0")),
      (VersionNumber("11.4-foo"), VersionNumber("12.0-foo"))
    )

    forAll(data) { (o, n) =>
      Version.nextMajor(o) shouldEqual n
    }
  }

  it should "increment minor version" in {
    val data = Table(
      ("old", "new"),
      (VersionNumber("1"), VersionNumber("1.1")),
      (VersionNumber("3.1"), VersionNumber("3.2")),
      (VersionNumber("1.0.3"), VersionNumber("1.1.0")),
      (VersionNumber("11.4-foo"), VersionNumber("11.5-foo"))
    )

    forAll(data) { (o, n) =>
      Version.nextMinor(o) shouldEqual n
    }
  }

  it should "increment build version" in {
    val data = Table(
      ("old", "new"),
      (VersionNumber("1"), VersionNumber("1.0.1")),
      (VersionNumber("3.1"), VersionNumber("3.1.1")),
      (VersionNumber("1.0.3-bar"), VersionNumber("1.0.4-bar")),
      (VersionNumber("11.4-foo"), VersionNumber("11.4.1-foo"))
    )

    forAll(data) { (o, n) =>
      Version.nextBuild(o) shouldEqual n
    }
  }

  it should "be able to compare two versions" in {
    val data = Table(
      ("x", "y", "compare"),
      (VersionNumber("1"), VersionNumber("2"), -1),
      (VersionNumber("3"), VersionNumber("2"), 1),
      (VersionNumber("1.0"), VersionNumber("1.0"), 0),
      (VersionNumber("1.1.0"), VersionNumber("1.2.0"), -1),
      (VersionNumber("1.1.1"), VersionNumber("1.1.0"), 1),
      (VersionNumber("1.1.1"), VersionNumber("1.1"), 1)
    )

    forAll(data) { (x, y, c) =>
      Version.versionOrdering.compare(x, y) shouldEqual c
    }
  }

}
