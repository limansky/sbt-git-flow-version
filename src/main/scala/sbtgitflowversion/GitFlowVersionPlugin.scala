package sbtgitflowversion

import com.typesafe.sbt.GitPlugin
import sbt.{AutoPlugin, Plugins}

object GitFlowVersionPlugin extends AutoPlugin {
  override def requires: Plugins = GitPlugin
}
