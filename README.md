# Gradle Enterprise Conventions

Conventions for Maven and Gradle projects that use the Gradle Enterprise instance hosted at [ge.spring.io](https://ge.spring.io).

## Build cache conventions

When applied, the conventions will configure the build cache to:

- Enable local caching.
- Use https://ge.spring.io/cache/ as the remote cache.
- Enable pulling from the remote cache.
- Enable pushing to the remote cache if the required credentials are available.

### Remote cache credentials

:rotating_light: **Credentials must not be configured in environments where pull requests are built.** :rotating_light:

Pushing to the remote cache requires authentication.
The necessary credentials can be provided using the `GRADLE_ENTERPRISE_CACHE_USERNAME` and `GRADLE_ENTERPRISE_CACHE_PASSWORD` environment variables.

#### Bamboo

The username and password environment variables should be set using `${bamboo.ge.cache.username}` and `${bamboo.ge.cache.password}` respectively.

#### Concourse

The username and password environment variables should be set using `((gradle_enterprise_cache_user.username))` and `((gradle_enterprise_cache_user.password))` from CredHub respectively.

#### Jenkins

The username and password environment variables should be set using the `gradle_enterprise_cache_user` username with password credential.

## Build scan conventions

When applied as a settings plugin (Gradle 6) or a project plugin (Gradle 5) alongside the [Gradle Enterprise Plugin](https://plugins.gradle.org/plugin/com.gradle.enterprise), the plugin will configure publishing of build scans to [ge.spring.io](https://ge.spring.io) when authenticated.
The build scans will be customized to:

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
 - Enable capturing of task (Gradle) or goal (Maven) input files
 - Upload build scans in the foreground when running on CI

### Build scan publishing credentials

:rotating_light: **Credentials must not be configured in environments where pull requests are built.** :rotating_light:

Publishing to [ge.spring.io](https://ge.spring.io) requires authentication via an access key.
When running on CI, the access key should be made available via the `GRADLE_ENTERPRISE_ACCESS_KEY` environment variable.

#### Bamboo

The environment variable should be set to `${bamboo.gradle_enterprise_secret_access_key}`.

#### Concourse

The environment variable should be set using `((gradle_enterprise_secret_access_key))` from CredHub.

#### Jenkins

The environment variable should be set using the `gradle_enterprise_secret_access_key` secret text credential.

#### Local

An access key can be provisioned by running `./gradlew provisionGradleEnterpriseAccessKey` once the project has been configured to use this plugin.

### Git branch names

`git rev-parse --abbrev-ref HEAD` is used to determine the name of the current branch.
This does not work on Concourse as its git resource places the repository in a detached head state.
To work around this, an environment variable named `BRANCH` can be set on the task to provide the name of the branch.

### Detecting CI

Bamboo is detected by looking for an environment variable named `bamboo_resultsUrl`.

Concourse does not automatically set any environment variables in the build's container that allow its use to be detected.
To work around this, an environment variable named `CI` can be set on the task.

Jenkins is detected by looking for an environment variable named `JENKINS_URL`.

## Using the conventions

The conventions are published to https://repo.spring.io.
Depending on the version you wish to use, they will be availble from the `snapshot` or `release` repository.

### Gradle

The first step in using the conventions is to make the necessary repository available for plugin resolution.
This is done by configuring a plugin management repository in `settings.gradle`, as shown in the following example:

```groovy
pluginManagement {
	repositories {
		gradlePluginPortal()
		maven { url 'https://repo.spring.io/release' }
	}
}
```

In the example above, `gradlePluginPortal()` is declared to allow other plugins to continue to be resolved from the portal.
The second step in applying the plugin depends on whether you are using Gradle 5 or 6.

#### Gradle 5.x

The plugin should be applied in `build.gradle` of the root project, alongside the `com.gradle.build-scan` plugin:

```groovy
plugins {
	// …
	id "com.gradle.build-scan" version "<<version>>"
	id "io.spring.ge" version "<<version>>"
	// …
}
```

#### Gradle 6.x

The plugin should be applied in `settings.gradle`, alongside the `com.gradle.enterprise` plugin:

```groovy
plugins {
	// …
	id "com.gradle.enterprise" version "<<version>>"
	id "io.spring.ge" version "<<version>>"
	// …
}
```

### Maven

To use the conventions, configure both the Gradle Enterprise and the Conventions Maven extensions in `.mvn/extensions.xml`, as showing in the following example:

```xml
<extensions>
    <extension>
        <groupId>com.gradle</groupId>
        <artifactId>gradle-enterprise-maven-extension</artifactId>
        <version><<version>></version>
    </extension>
    <extension>
        <groupId>io.spring.ge</groupId>
        <artifactId>gradle-enterprise-conventions-maven-extension</artifactId>
        <version><<version>></version>
    </extension>
</extensions>
```