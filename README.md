# Gradle Enterprise Conventions Plugin

A Gradle plugin for configuring projects to use the Gradle Enterprise instance hosted at [https://ge.spring.io](https://ge.spring.io).
When applied, use of the build cache hosted at https://ge.spring.io/ is enabled.
When the [Gradle Enterprise Plugin](https://plugins.gradle.org/plugin/com.gradle.enterprise) is also applied, build scans are configured to use Spring conventions.

## Build cache conventions

When applied, the plugin will configure the build cache to:

- Enable local caching.
- Enable pulling from the remote cache.
- Enable pusing to the remote cache if the `GRADLE_ENTERPRISE_CACHE_USERNAME` and `GRADLE_ENTERPRISE_CACHE_PASSWORD` environment variables are set.

## Build scan conventions

When applied alongside the Gradle Enterprise plugin, the plugin will configure build scans to:

- Add tags:
    - `JDK-<version>`, where `version` is the specification version of the JDK running the build.
    - `CI` or `Local` depending on where the build is executing.
    - `dirty` if the git working copy is dirty.
    - Name of the git branch being built.
- Add custom key-value pairs:
    - `Git branch` with a value of the name of the git branch being built.
    - `Git commit` with a value of the commit ID `HEAD`
    - `Git status` when the working copy is dirty.
      The value is the output of `git status --porcelain`.
 - Add links:
    - `CI build` when building on Bamboo, linking to the build on Bamboo.
    - `Git commit build scans`, links to scans for other builds of the same git commit.

### Git branch names

`git rev-parse --abbrev-ref HEAD` is used to determine the name of the current branch.
This does not work on Concourse as its git resource places the repository in a detached head state.
To work around this, an environment variable named `BRANCH` can be set on the task to provide the name of the branch.

### Detecting CI

Bamboo is detected by looking for an environment variable named `bamboo_resultsUrl`.

Concourse does not set any environment variables in the build's container that allow its use to be detected.
To work around this, an environment variable named `CI` can be set on the task.