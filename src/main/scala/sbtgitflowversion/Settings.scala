package sbtgitflowversion

case class Settings(
  initialVersion: String,
  tagPrefix: String,
  tagSuffix: String
) {
  def tagFilter(tagName: String): Boolean = {
    tagName.startsWith(tagPrefix) &&
      tagName.endsWith(tagSuffix) &&
      tagName.length > tagPrefix.length + tagSuffix.length
  }
}
