package sbtgitflowversion

case class Version(major: Int, minor: Option[Int], build: Option[Int], suffix: Option[String]) {

  def nextMajor: Version = Version(major + 1, minor map (_ => 0), build map (_ => 0), suffix)

  def nextMinor: Version = Version(major, minor.map(_ + 1) orElse Some(1), build map (_ => 0), suffix)

  def nextBuild: Version = Version(major, minor orElse Some(0), build.map(_ + 1) orElse Some(1), suffix)

  override def toString: String = {
    val min = minor map (x => s".$x") getOrElse ""
    val bld = build map (x => s".$x") getOrElse ""
    val sfx = suffix map(x => s"-$x") getOrElse ""
    s"$major$min$bld$sfx"
  }
}

object Version {

  def parse(s: String): Option[Version] = {
    val pattern = "^(\\d+)".r

    s match {
      case pattern(maj) => Some(Version(maj.toInt, None, None, None))
      case _ => None
    }
  }

}
