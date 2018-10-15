package sbtgitflowversion

abstract class NamePolicy extends ((String, String, Seq[String]) => Either[String, String])

object NamePolicy {
  val SNAPSHOT = "SNAPSHOT"

  def lastTag = new NamePolicy {
    override def apply(b: String, l: String, c: Seq[String]): Either[String, String] = Right(s"$l-$SNAPSHOT")
  }

  def currentTag = new NamePolicy {
    override def apply(b: String, l: String, c: Seq[String]): Either[String, String] = {
      if (c.isEmpty) {
        Left("No tag defined for current version")
      } else if (c.size > 1) {
        val allTags = c.mkString(", ")
        Left(s"Too many tags: $allTags")
      } else {
        Right(c.head)
      }
    }
  }

  def nextTag = new NamePolicy {
    override def apply(b: String, l: String, c: Seq[String]): Either[String, String] = ???
  }

  def unknownVersion = new NamePolicy {
    override def apply(b: String, l: String, c: Seq[String]): Either[String, String] = {
      val tags = c.mkString(", ")
      Left(s"Don't know how to calculate version for branch $b, last version $l, tags: $tags")
    }
  }

  val defaultPolicy = (branch: String) => branch match {
    case "master" => currentTag

    case "develop" => nextTag

//    case x if x.startsWith("release/") => extractVersion

    case x => unknownVersion
  }

}
