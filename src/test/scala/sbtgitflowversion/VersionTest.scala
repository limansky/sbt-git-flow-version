package sbtgitflowversion

import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers, OptionValues}

class VersionTest extends FlatSpec with Matchers with OptionValues with TableDrivenPropertyChecks {

  "Version" should "parse string" in {
    val data = Table(
      ("String", "expected"),
      ("1", Version(1, None, None, None))
    )

    forAll(data) { (s, e)  =>
      Version.parse(s).value shouldEqual e
    }
  }

  it should "increment major version" in {
    val data = Table(
      ("old", "new"),
      (Version(1, None, None, None), Version(2, None, None, None)),
      (Version(3, Some(1), None, None), Version(4, Some(0), None, None)),
      (Version(1, Some(0), Some(3), None), Version(2, Some(0), Some(0), None)),
      (Version(11, Some(4), None, Some("foo")), Version(12, Some(0), None, Some("foo")))
    )

    forAll(data) { (o, n) =>
      o.nextMajor shouldEqual n
    }
  }

  it should "increment minor version" in {
    val data = Table(
      ("old", "new"),
      (Version(1, None, None, None), Version(1, Some(1), None, None)),
      (Version(3, Some(1), None, None), Version(3, Some(2), None, None)),
      (Version(1, Some(0), Some(3), None), Version(1, Some(1), Some(0), None)),
      (Version(11, Some(4), None, Some("foo")), Version(11, Some(5), None, Some("foo")))
    )

    forAll(data) { (o, n) =>
      o.nextMinor shouldEqual n
    }
  }

  it should "increment build version" in {
    val data = Table(
      ("old", "new"),
      (Version(1, None, None, None), Version(1, Some(0), Some(1), None)),
      (Version(3, Some(1), None, None), Version(3, Some(1), Some(1), None)),
      (Version(1, Some(0), Some(3), Some("bar")), Version(1, Some(0), Some(4), Some("bar"))),
      (Version(11, Some(4), None, Some("foo")), Version(11, Some(4), Some(1), Some("foo")))
    )

    forAll(data) { (o, n) =>
      o.nextBuild shouldEqual n
    }
  }

}
