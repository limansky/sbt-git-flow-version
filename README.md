sbt-git-flow-version
====================

**sbt-git-flow-version** is an sbt plugin for automation git flow based versioning.

[![Build Status](https://travis-ci.org/limansky/sbt-git-flow-version.svg?branch=master)](https://travis-ci.org/limansky/sbt-git-flow-version)

Motivation
----------

If you using git flow for your projects development you possible know that it is
quite annoying to switch between different branches and with different versions.
This plugin allows you to define different versioning rules for different
branches and change versions automatically without changing your code.

Requirements
------------

Java 8 and sbt 0.13.5+ or 1.x.  This plugin depends on sbt-git.

Installation
------------

> NOTE: At the moment there is no stable version yet.  The plugin is under active development.

```Scala
addSbtPlugin("me.limansky" % "sbt-git-flow-vesion" % "0.1-SNAPSHOT")
```

Configuration
-------------

The heart of the plugin configuration is a `policy` setting which defines version rules as a
tuple of `BranchMatcher` and `VersionPolicy`.  Let's start with branch matchers.  Branch matcher
is a function taking branch name and returning `Option[Matching]`. Matching is a class containing
matched branch name and optional extraction.  There are number of built-in matchers:

  - `exact(name: String)` - branch name is equals to `name`
  - `prefix(prefix: String)` - branch name starts with `prefix`.  The part after prefix is extraction.
  - `prefixes(prefixes: String*)` - same as prefix but supports a number of prefixes.
  - `regex(r: String)` - branch name matches regular expression `r`.  If the expression contains
    groups, the first group will be returned as extraction.
  - `any` - matches any branch (might be used to define default behaviour).


So, the default policy is:

```Scala
Seq(
  exact("master") -> currentTag,
  exact("develop") -> nextMinor,
  prefix("release/") -> matching,
  prefixes("feature/", "bugfix/", "hotfix/") -> lastTagWithMatching,
  any -> unknownVersion
)
```

Which can be described as following rules:

  - If branch name is "master" then the branch name is a current commit tag
  - If branch name is "develop" then the branch name is a next minor version.
    E.g. if last version was `1.4.2`, the current version is `1.5.0-SNAPSHOT`.
  - If the branch name starts with "release/" then the version is taken from the
    branch name.  E.g. for the branch "release/2.12.85" the version is "2.12.85-SNAPSHOT".
  - For the branches started with "feature/", "bugfix/", and "hotfix/" the version is
    a combination of the last version and matching.  E.g. for the branch "feature/123-new-ui"
    and the prevous version "1.0.1" the current version is "1.0.1-123-new-ui-SNAPSHOT".
  - Finally, if the branch name doesn't follow any of these rules, the build fails,
    because version is unknown.
