package sbtgitflowversion

case class CurrentRevision(branchName: String, lastTag: Option[String], currentTags: Seq[String])
