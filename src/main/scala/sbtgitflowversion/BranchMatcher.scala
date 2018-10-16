package sbtgitflowversion

case class Matching(branch: String, extraction: String)

trait BranchMatcher extends (String => Option[Matching])

object BranchMatcher {

  val any: BranchMatcher = new BranchMatcher {
    override def apply(branch: String): Option[Matching] = Some(Matching(branch, ""))
  }

  def exact(name: String): BranchMatcher = new BranchMatcher {
    override def apply(branch: String): Option[Matching] = {
      if (name == branch) Some(Matching(branch, "")) else None
    }
  }

  def prefix(p: String): BranchMatcher = new BranchMatcher {
    override def apply(branch: String): Option[Matching] = {
      if (branch startsWith p) {
        Some(Matching(branch, branch.substring(p.length)))
      } else None
    }
  }

  def prefixes(ps: String*): BranchMatcher = new BranchMatcher {
    override def apply(branch: String): Option[Matching] = {
      ps.find(branch.startsWith).map(p => Matching(branch, branch.substring(p.length)))
    }
  }
}
