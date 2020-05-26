package sbtgitflowversion

import sbt.VersionNumber

trait TagMatcher extends (String => Option[VersionNumber])

object TagMatcher {
  val raw: TagMatcher = new TagMatcher {
    override def apply(tag: String): Option[VersionNumber] = Version.parse(tag)
  }

  def prefix(p: String): TagMatcher = prefixAndSuffix(p, "")

  def suffix(s: String): TagMatcher = prefixAndSuffix("", s)

  def prefixAndSuffix(p: String, s: String): TagMatcher =
    new TagMatcher {
      override def apply(tag: String): Option[VersionNumber] = {
        if (tag.startsWith(p) && tag.endsWith(s)) {
          Version.parse(tag.substring(p.length, tag.length - s.length))
        } else {
          None
        }
      }
    }
}
