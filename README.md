# Develocity Conventions

Conventions for Maven and Gradle projects that use the Develocity instance hosted at [ge.spring.io](https://ge.spring.io).

## Build cache conventions

When applied, the conventions will configure the build cache to:

- Enable local caching.
- Use https://ge.spring.io as the remote cache server.
- Enable pulling from the remote cache.
- Enable pushing to the remote cache when a CI environment is detected and the required access token is available.

### Remote cache

#### URL

By default, https://ge.spring.io will be used as the remote cache server.
The server can be configured using the `DEVELOCITY_CACHE_SERVER` environment variable.
For backwards compatibility, `GRADLE_ENTERPRISE_CACHE_URL` is also supported for a limited time.
`/cache/` is removed from the end of the URL and the remainder is used to configure the remote cache server.

## Build scan conventions

When applied alongside the [Develocity Plugin](https://plugins.gradle.org/plugin/com.gradle.develocity), the plugin will configure publishing of build scans to [ge.spring.io](https://ge.spring.io) when authenticated.
The build scans will be customized to:

- Add tags:
    - `JDK-<version>`.
      When using Maven, `<version>` is the specification version of the JDK running the build.
      When using Gradle, `<version>` is the value of the `toolchainVersion` project property or, when not set, it's the specification version of the JDK running the build.
    - `CI` or `Local` depending on where the build is executing.
    - `dirty` if the git working copy is dirty.
    - Name of the git branch being built.
- Add custom key-value pairs:
    - `Git branch` with a value of the name of the git branch being built.
    - `Git commit` with a value of the commit ID `HEAD`
    - `Git status` when the working copy is dirty.
      The value is the output of `git status --porcelain`.
    - `Docker` when the `docker` CLI is available.
      The value is the output of `docker --version`.
    - `Docker Compose` when `docker compose` CLI is available.
      The value is the output of `docker compose version`.
 - Add links:
    - `CI build` when building on Bamboo, GitHub Actions, or Jenkins, linking to the build on the CI server.
    - `Git commit build scans`, linking to scans for other builds of the same git commit.
 - Enable capturing of file fingerprints
 - Upload build scans in the foreground when running on CI

### Git branch names

`git rev-parse --abbrev-ref HEAD` is used to determine the name of the current branch.
This does not work on Concourse as its git resource places the repository in a detached head state.
To work around this, an environment variable named `BRANCH` can be set on the task to provide the name of the branch.

### Anonymous publication

When using Gradle, build scans can be published anonymously to scans.gradle.com by running the build with `--scan`.

## Authentication

:rotating_light: **Credentials must not be configured in environments where pull requests are built.** :rotating_light:

Publishing build scans and pushing to the remote cache requires authentication via an access key.
Additionally, pushing to the remote cache also requires that a CI environment be detected.
When running on CI, the access key should be made available via the `DEVELOCITY_ACCESS_KEY` environment variable.
`GRADLE_ENTERPRISE_ACCESS_KEY` can also be used although it will result in a deprecation warning from Gradle.

#### Bamboo

The environment variable should be set to `${bamboo.gradle_enterprise_secret_access_key}`.

#### Concourse

The environment variable should be set using `((gradle_enterprise_secret_access_key))` from Vault.

#### GitHub Actions

The environment variable should be set using the `GRADLE_ENTERPRISE_SECRET_ACCESS_KEY` organization secret.

#### Jenkins

The environment variable should be set using the `gradle_enterprise_secret_access_key` secret text credential.

#### Local

An access key can be provisioned by running `./gradlew provisionDevelocityAccessKey` once the project has been configured to use this plugin.

## Detecting CI

Bamboo is detected by looking for an environment variable named `bamboo_resultsUrl`.

Concourse does not automatically set any environment variables in the build's container that allow its use to be detected.
To work around this, an environment variable named `CI` can be set on the task.

Jenkins is detected by looking for an environment variable named `JENKINS_URL`.

## Using the conventions

Releases of the conventions are published to Maven Central.
Snapshots are published to https://repo.spring.io/snapshot.

### Gradle

The conventions support Gradle 7.4 and later.

The first step in using the conventions is to make the necessary repository available for plugin resolution.
This is done by configuring a plugin management repository in `settings.gradle`, as shown in the following example:

```groovy
pluginManagement {
	repositories {
		gradlePluginPortal()
		mavenCentral()
	}
}
```

In the example above, `gradlePluginPortal()` is declared to allow other plugins to continue to be resolved from the portal.

Now apply the plugin in `settings.gradle`, alongside the `com.gradle.develocity` plugin:

```groovy
plugins {
	// …
	id "com.gradle.develocity" version "<<version>>"
	id "io.spring.develocity.conventions" version "<<version>>"
	// …
}
```

### Maven

To use the conventions, create a `.mvn/extensions.xml` file in the root of the project:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<extensions>
        <extension>
                <groupId>io.spring.develocity.conventions</groupId>
                <artifactId>develocity-conventions-maven-extension</artifactId>
                <version><<version>></version>
        </extension>
</extensions>
```

Any existing `.mvn/gradle-enterprise.xml` file should be deleted in favor of the configuration that's provided by the conventions.
Lastly, add `.mvn/.develocity/` to the project's `.gitignore` file.
The conventions are ready to use.
