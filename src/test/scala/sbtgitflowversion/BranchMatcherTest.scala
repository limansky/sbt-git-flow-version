package sbtgitflowversion

import org.scalatest.{FlatSpec, Matchers, OptionValues}

class BranchMatcherTest extends FlatSpec with Matchers with OptionValues {

  import BranchMatcher._

  "BranchMatcher" should "match any branch" in {
    any("foo").value shouldEqual Matching("foo", "")
  }

  it should "match branch by name" in {
    exact("foo")("foo").value shouldEqual Matching("foo", "")
    exact("foo")("fooo") shouldEqual None
  }

  it should "match branch by prefix" in {
    prefix("release/")("release/1.0.0").value shouldEqual Matching("release/1.0.0", "1.0.0")
    prefix("foo")("fooo").value shouldEqual Matching("fooo", "o")
    prefix("foo")("bar") shouldEqual None
  }

  it should "match branch by list of prefixes" in {
    val m = prefixes("bugfix/", "feature/", "hotfix/")

    m("feature/321-wow").value shouldEqual Matching("feature/321-wow", "321-wow")
    m("megafix/321-wow") shouldEqual None
  }
}
