package sbtgitflowversion

import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BranchMatcherTest extends AnyFlatSpec with Matchers with OptionValues {

  import BranchMatcher._

  "BranchMatcher" should "match any branch" in {
    any("foo").value shouldEqual Matching("foo")
  }

  it should "match branch by name" in {
    exact("foo")("foo").value shouldEqual Matching("foo")
    exact("foo")("fooo") shouldEqual None
  }

  it should "match branch by prefix" in {
    prefix("release/")("release/1.0.0").value shouldEqual Matching("release/1.0.0", Some("1.0.0"))
    prefix("foo")("fooo").value shouldEqual Matching("fooo", Some("o"))
    prefix("foo")("bar") shouldEqual None
  }

  it should "match branch by list of prefixes" in {
    val m = prefixes("bugfix/", "feature/", "hotfix/")

    m("feature/321-wow").value shouldEqual Matching("feature/321-wow", Some("321-wow"))
    m("megafix/321-wow") shouldEqual None
  }

  it should "match branch by regex" in {
    val m = BranchMatcher.regex("^be[ea]r".r)

    m("beer-18").value shouldEqual Matching("beer-18")
    m("bearing").value shouldEqual Matching("bearing")
    m("boar-11") shouldEqual None
  }

  it should "capture from regex" in {
    val m = BranchMatcher.regex("feature/([^-]+)-fix".r)
    m("feature/544-fix").value shouldEqual Matching("feature/544-fix", Some("544"))
    m("feature/544-fax") shouldEqual None
  }
}
