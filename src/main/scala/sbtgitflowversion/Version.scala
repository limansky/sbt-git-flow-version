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
        val post = Seq.fill(ns.length - n - 1)(0l)
        VersionNumber((pre :+ (ns(n) + 1)) ++ post, versionNumber.tags, versionNumber.extras)
      } else {
        val post = Seq.fill(n - ns.length)(0l)
        VersionNumber(ns ++ post :+ 1l, versionNumber.tags, versionNumber.extras)
      }

    } else {
      versionNumber
    }
  }

}
