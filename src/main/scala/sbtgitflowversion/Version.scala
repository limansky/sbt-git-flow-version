package sbtgitflowversion

import sbt.VersionNumber

import scala.util.Try

object Version {

  def parse(s: String): Option[VersionNumber] = {
    Try(VersionNumber(s)).filter(_.numbers.nonEmpty).toOption
  }

  def nextMajor(version: VersionNumber): VersionNumber = {
    next(0)(version)
  }

  def nextMinor(version: VersionNumber): VersionNumber = {
    next(1)(version)
  }

  def nextBuild(version: VersionNumber): VersionNumber = {
    next(2)(version)
  }

  def next(n: Int)(versionNumber: VersionNumber): VersionNumber = {
    val ns = versionNumber.numbers
    if (ns.nonEmpty) {
      if (n < ns.length) {
        val pre = ns.take(n)
        val post = Seq.fill(ns.length - n - 1)(0L)
        VersionNumber((pre :+ (ns(n) + 1)) ++ post, versionNumber.tags, versionNumber.extras)
      } else {
        val post = Seq.fill(n - ns.length)(0L)
        VersionNumber(ns ++ post :+ 1L, versionNumber.tags, versionNumber.extras)
      }

    } else {
      versionNumber
    }
  }

  val versionOrdering: Ordering[VersionNumber] = new Ordering[VersionNumber] {
    override def compare(x: VersionNumber, y: VersionNumber): Int = {
      val pair = x.numbers
        .zipAll(y.numbers, 0L, 0L)
        .find(p => java.lang.Long.compare(p._1, p._2) != 0)

      pair.map(p => java.lang.Long.compare(p._1, p._2)).getOrElse(0)
    }
  }

}
