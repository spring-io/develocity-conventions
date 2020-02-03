# Gradle Enterprise Conventions Plugin

A Gradle plugin for configuring projects to use the Gradle Enterprise instance hosted at [ge.spring.io](https://ge.spring.io).

## Build cache conventions

When applied and the build cache is enabled (`org.gradle.caching=true` in `gradle.properties`), the plugin will configure the build cache to:

- Enable local caching.
- Use https://ge.spring.io/cache/ as the remote cache.
- Enable pulling from the remote cache.
- Enable pushing to the remote cache if the required credentials are available.

### Remote cache credentials

**Credentials must not be configured in environments where pull requests are built.**

Pushing to the remote cache requires authentication.
The necessary credentials can be provided using the `GRADLE_ENTERPRISE_CACHE_USERNAME` and `GRADLE_ENTERPRISE_CACHE_PASSWORD` environment variables.
On Bamboo, the username and password environment variables should be set using `${bamboo.ge.cache.username}` and `${bamboo.ge.cache.password}` respectively.
On Concourse, the username and password environment variables should be set using `((gradle_enterprise_cache_user.username))` and `((gradle_enterprise_cache_user.password))` from CredHub respectively.
On Jeninks, the username and password environment variables should be set using the `gradle_enterprise_cache_user` username with password credential.

## Build scan conventions

When applied alongside the [Gradle Enterprise Plugin](https://plugins.gradle.org/plugin/com.gradle.enterprise), the plugin will configure build scans to:

- Publish to [ge.spring.io](https://ge.spring.io)
- Add tags:
    - `JDK-<version>`, where `<version>` is the specification version of the JDK running the build.
    - `CI` or `Local` depending on where the build is executing.
    - `dirty` if the git working copy is dirty.
    - Name of the git branch being built.
- Add custom key-value pairs:
    - `Git branch` with a value of the name of the git branch being built.
    - `Git commit` with a value of the commit ID `HEAD`
    - `Git status` when the working copy is dirty.
      The value is the output of `git status --porcelain`.
 - Add links:
    - `CI build` when building on Bamboo or Jenkins, linking to the build on the CI server.
    - `Git commit build scans`, linking to scans for other builds of the same git commit.

### Build scan publishing credentials

**Credentials must not be configured in environments where pull requests are built.**

Publishing to [ge.spring.io](https://ge.spring.io) requires authentication via an access key.
When running on CI, the access key should be made available via the `GRADLE_ENTERPRISE_ACCESS_KEY` environment variable.
On Bamboo, the environment variable should be set to `${bamboo.gradle_enterprise_secret_access_key}`.
On Concourse, the environment variable should be set using `((gradle_enterprise_secret_access_key))` from CredHub.
On Jenkins, the environment variable should be set using the `gradle_enterprise_secret_access_key` secret text credential.
When running locally, an access key can be provisioned by running `./gradlew provisionGradleEnterpriseAccessKey` once the project has been configured to use this plugin.

### Git branch names

`git rev-parse --abbrev-ref HEAD` is used to determine the name of the current branch.
This does not work on Concourse as its git resource places the repository in a detached head state.
To work around this, an environment variable named `BRANCH` can be set on the task to provide the name of the branch.

### Detecting CI

Bamboo is detected by looking for an environment variable named `bamboo_resultsUrl`.

Concourse does not automatically set any environment variables in the build's container that allow its use to be detected.
To work around this, an environment variable named `CI` can be set on the task.

Jenkins is detected by looking for an environment variable named `JENKINS_URL`.