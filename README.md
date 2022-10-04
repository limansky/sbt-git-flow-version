sbt-git-flow-version
====================

**sbt-git-flow-version** is an sbt plugin for automation git flow based versioning.

[![Build Status](https://github.com/limansky/sbt-git-flow-version/actions/workflows/ci.yml/badge.svg)](https://github.com/limansky/sbt-git-flow-version/actions/workflows/ci.yml)

Motivation
----------

If you using git flow for your projects development you possible know that it is
quite annoying to switch between different branches and with different versions.
This plugin allows you to define different versioning rules for different
branches and change versions automatically without changing your code.

Requirements
------------

Java 8 and sbt 1.x.  This plugin depends on sbt-git.

  Note: if you are still using sbt 0.13.5+, you can use `sbt-git-flow-version` 0.3 or eariler.

Installation
------------

```Scala
addSbtPlugin("me.limansky" % "sbt-git-flow-version" % "0.4")
```

Configuration
-------------

### Version policy

The heart of the plugin configuration is a `versionPolicy` setting which defines version rules as a
tuple of `BranchMatcher` and `VersionCalculator`.  Let's start with branch matchers.  Branch matcher
is a function taking branch name and returning `Option[Matching]`. Matching is a class containing
matched branch name and optional extraction.  There are number of built-in matchers:

  - `exact(name: String)` - branch name is equals to `name`
  - `prefix(prefix: String)` - branch name starts with `prefix`.  The part after prefix is extraction.
  - `prefixes(prefixes: String*)` - same as prefix but supports a number of prefixes.
  - `regex(r: String)` - branch name matches regular expression `r`.  If the expression contains
    groups, the first group will be returned as extraction.
  - `any` - matches any branch (might be used to define default behaviour).

There are also several built in implementations for `VersionCalculator`:

  - `currentTag` - take a version from a current tag. If there are several current tags it will take the
    one with a maximal version.
  - `nextMajor`, `nextMinor`, `nextBuild`, `nextN` - increment major, minor, build or n-th number of
    a last version. The version is snapshot by default.
  - `nextGlobalMajor`, `nextGlobalMinor`, `nextGlobalBuild`, `nextGlobalN` - same as `nextX`, but trying to
    find maximal available version not only merged to current branch, but even for not merged and remote branches.
  - `matching` - take a version from matching returned by `BranchMatcher`. The version is snapshot by default.
  - `lastVersion` - previous version.  Version value is taken from last tag or `initialVersion` setting.
    The version is not snapshot by default.
  - `lastVersionWithMatching` - takes last version and append matching returned by `BranchMatcher`.  By default
    new version is snapshot.
  - `lastVersionWithSuffix` - takes fixed suffix to append to the last version.  By default new version is snapshot.
  - `fixed` - takes flag if the version is snapshot and numbers.  Just fixed version.
  - `unknownVersion` - fails with unknown version message.

  Note: calculating global version might take a long time if you have a lot of branches, so, to speed up the process
  it doesn't take in account all branches, but only the ones for those `VersionCalculator` has a global version flag.
  By default it `matching` policy.  This means, that by default when calculating `develop` version it use tags and
  and `release/xxx` branches only.

So, the default policy is:

```Scala
Seq(
  exact("master") -> currentTag(),
  exact("develop") -> nextGlobalMinor(),
  prefix("release/") -> matching(),
  prefixes("feature/", "bugfix/", "hotfix/") -> lastVersionWithMatching(),
  any -> unknownVersion
)
```

Which can be described as following rules:

  - If branch name is "master" then the branch name is a current commit tag
  - If branch name is "develop" then the branch name is a next global minor version.
    E.g. if last version was `1.4.2`, the current version is `1.5.0-SNAPSHOT`.
  - If the branch name starts with "release/" then the version is taken from the
    branch name.  E.g. for the branch "release/2.12.85" the version is "2.12.85-SNAPSHOT".
  - For the branches started with "feature/", "bugfix/", and "hotfix/" the version is
    a combination of the last version and matching.  E.g. for the branch "feature/123-new-ui"
    and the prevous version "1.0.1" the current version is "1.0.1-123-new-ui-SNAPSHOT".
  - Finally, if the branch name doesn't follow any of these rules, the build fails,
    because version is unknown.

### Tag matcher

By default `sbt-git-flow-version` expects version in the tags as is (e.g. "1.0.1").
You can change this behaviour if you are using some prefixes or/and suffixes in your
tag names.  To do that use `tagMatcher` setting with `TagMatcher` class.  Following
tag matchers are available:

  - `raw` - default tag matcher, takes the version as is.
  - `prefix(p: String)` - tag should be started with prefix `p` (e.g. `prefix("Version_")`
    matches version 3.2.1 from tag "Version_3.2.1").
  - `suffix(s: String)` - tag should be ended with suffix `s`.
  - `prefixAndSuffix(p: String, s:String)` - tag should start with prefix `p` and end with
    suffix `s`.
