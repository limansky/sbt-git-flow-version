package sbtgitflowversion

import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{ FlatSpec, Matchers }
import sbt.VersionNumber

class TagMatcherTest extends FlatSpec with Matchers with TableDrivenPropertyChecks {

  "TagMatcher" should "match raw version" in {
    val cases = Table(
      ("tag", "version"),
      ("1.2.3", Some(VersionNumber("1.2.3"))),
      ("42.10", Some(VersionNumber("42.10"))),
      ("foo", None)
    )

    forAll(cases) { (tag, expected) =>
      TagMatcher.raw(tag) shouldEqual expected
    }
  }

  it should "match by prefix" in {
    val cases = Table(
      ("tag", "version"),
      ("1.2.3", None),
      ("version_42.10", Some(VersionNumber("42.10"))),
      ("version_42.10-FINAL", Some(VersionNumber("42.10-FINAL"))),
      ("foo", None),
      ("version_foo", None)
    )

    forAll(cases) { (tag, expected) =>
      TagMatcher.prefix("version_")(tag) shouldEqual expected
    }
  }

  it should "match by suffix" in {
    val cases = Table(
      ("tag", "version"),
      ("1.2.3", None),
      ("42.10-GA", Some(VersionNumber("42.10"))),
      ("foo", None),
      ("foo-GA", None)
    )

    forAll(cases) { (tag, expected) =>
      TagMatcher.suffix("-GA")(tag) shouldEqual expected
    }
  }

  it should "match by prefix and suffix" in {
    val cases = Table(
      ("tag", "version"),
      ("1.2.3", None),
      ("Version_1.2.3", None),
      ("1.2.3-GA", None),
      ("Version_42.10-GA", Some(VersionNumber("42.10"))),
      ("foo", None),
      ("foo-GA", None),
      ("Version_foo-GA", None)
    )

    forAll(cases) { (tag, expected) =>
      TagMatcher.prefixAndSuffix("Version_", "-GA")(tag) shouldEqual expected
    }
  }
}
