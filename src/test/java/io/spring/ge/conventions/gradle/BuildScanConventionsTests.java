/*
 * Copyright 2020-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.ge.conventions.gradle;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gradle.develocity.agent.gradle.scan.BuildScanPublishingConfiguration.PublishingContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link BuildScanConventions}.
 *
 * @author Andy Wilkinson
 */
class BuildScanConventionsTests {

	private final TestProcessRunner processRunner = new TestProcessRunner();

	private final TestDevelocityConfiguration develocity = new TestDevelocityConfiguration();

	private final TestBuildScanConfiguration buildScan = new TestBuildScanConfiguration();

	@Test
	void capturingOfFileFingerprintsIsEnabled() {
		new BuildScanConventions(this.develocity, this.processRunner).execute(this.buildScan);
		assertThat(this.buildScan.captureSettings.getFileFingerprints().get()).isTrue();
	}

	@Test
	void ipAddressesAreObfuscated() throws UnknownHostException {
		new BuildScanConventions(this.develocity, this.processRunner).execute(this.buildScan);
		assertThat(this.buildScan.obfuscation.ipAddressesObfuscator).isNotNull();
		List<String> obfuscatedAddresses = this.buildScan.obfuscation.ipAddressesObfuscator
			.apply(Arrays.asList(InetAddress.getByName("10.0.0.1"), InetAddress.getByName("10.0.0.2")));
		assertThat(obfuscatedAddresses).containsExactly("0.0.0.0", "0.0.0.0");
	}

	@Test
	void buildScansAreConfiguredToAlwaysPublishWhenAuthenticated() {
		new BuildScanConventions(this.develocity, this.processRunner).execute(this.buildScan);
		PublishingContext context = mock(PublishingContext.class);
		given(context.isAuthenticated()).willReturn(true);
		assertThat(this.buildScan.publishing.predicate.isSatisfiedBy(context)).isTrue();
		given(context.isAuthenticated()).willReturn(false);
		assertThat(this.buildScan.publishing.predicate.isSatisfiedBy(context)).isFalse();
	}

	@Test
	void buildScansAreConfiguredToPublishToGeSpringIo() {
		new BuildScanConventions(this.develocity, this.processRunner).execute(this.buildScan);
		assertThat(this.develocity.getServer().get()).isEqualTo("https://ge.spring.io");
	}

	@Test
	void whenBambooResultEnvVarIsPresentThenBuildScanIsTaggedWithCiNotLocal() {
		new BuildScanConventions(this.develocity, this.processRunner,
				Collections.singletonMap("bamboo_resultsUrl", "https://bamboo.exampl.com"))
			.execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("CI").doesNotContain("Local");
	}

	@Test
	void whenBambooResultEnvVarIsPresentThenBuildScanHasACiBuildLinkToIt() {
		new BuildScanConventions(this.develocity, this.processRunner,
				Collections.singletonMap("bamboo_resultsUrl", "https://bamboo.example.com"))
			.execute(this.buildScan);
		assertThat(this.buildScan.links).containsEntry("CI build", "https://bamboo.example.com");
	}

	@Test
	void whenBambooResultEnvVarIsPresentThenBuildScanHasBambooAsTheCiProviderValue() {
		new BuildScanConventions(this.develocity, this.processRunner,
				Collections.singletonMap("bamboo_resultsUrl", "https://bamboo.example.com"))
			.execute(this.buildScan);
		assertThat(this.buildScan.values).containsEntry("CI provider", "Bamboo");
	}

	@Test
	void whenCircleBuildUrlEnvVarIsPresentThenBuildScanIsTaggedWithCiNotLocal() {
		new BuildScanConventions(this.develocity, this.processRunner,
				Collections.singletonMap("CIRCLE_BUILD_URL", "https://circleci.example.com/gh/org/project/123"))
			.execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("CI").doesNotContain("Local");
	}

	@Test
	void whenCircleBuildUrlEnvVarIsPresentThenBuildScanHasACiBuildLinkToIt() {
		new BuildScanConventions(this.develocity, this.processRunner,
				Collections.singletonMap("CIRCLE_BUILD_URL", "https://circleci.example.com/gh/org/project/123"))
			.execute(this.buildScan);
		assertThat(this.buildScan.links).containsEntry("CI build", "https://circleci.example.com/gh/org/project/123");
	}

	@Test
	void whenCircleBuildUrlEnvVarIsPresentThenBuildScanHasCircleCiAsTheCiProviderValue() {
		new BuildScanConventions(this.develocity, this.processRunner,
				Collections.singletonMap("CIRCLE_BUILD_URL", "https://circleci.example.com/gh/org/project/123"))
			.execute(this.buildScan);
		assertThat(this.buildScan.values).containsEntry("CI provider", "CircleCI");
	}

	@Test
	void whenJenkinsUrlEnvVarIsPresentThenBuildScanIsTaggedWithCiNotLocal() {
		new BuildScanConventions(this.develocity, this.processRunner,
				Collections.singletonMap("JENKINS_URL", "https://jenkins.example.com"))
			.execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("CI").doesNotContain("Local");
	}

	@Test
	void whenJenkinsUrlAndBuildUrlEnvVarsArePresentThenBuildScanHasACiBuildLinkToBuildUrl() {
		Map<String, String> env = new HashMap<>();
		env.put("JENKINS_URL", "https://jenkins.example.com");
		env.put("BUILD_URL", "https://jenkins.example.com/builds/123");
		new BuildScanConventions(this.develocity, this.processRunner, env).execute(this.buildScan);
		assertThat(this.buildScan.links).containsEntry("CI build", "https://jenkins.example.com/builds/123");
	}

	@Test
	void whenJenkinsUrlEnvVarIsPresentThenBuildScanHasJenkinsAsTheCiProviderValue() {
		new BuildScanConventions(this.develocity, this.processRunner,
				Collections.singletonMap("JENKINS_URL", "https://jenkins.example.com"))
			.execute(this.buildScan);
		assertThat(this.buildScan.values).containsEntry("CI provider", "Jenkins");
	}

	@Test
	void whenCiEnvVarIsPresentThenBuildScanIsTaggedWithCiNotLocal() {
		new BuildScanConventions(this.develocity, this.processRunner, Collections.singletonMap("CI", null))
			.execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("CI").doesNotContain("Local");
	}

	@Test
	void whenCiEnvVarIsPresentThenBuildScanHasConcourseAsTheCiProviderValue() {
		new BuildScanConventions(this.develocity, this.processRunner, Collections.singletonMap("CI", null))
			.execute(this.buildScan);
		assertThat(this.buildScan.values).containsEntry("CI provider", "Concourse");
	}

	@Test
	void whenGitHubActionsEnvVarIsPresentThenBuildScanIsTaggedWithCiNotLocal() {
		new BuildScanConventions(this.develocity, this.processRunner,
				Collections.singletonMap("GITHUB_ACTIONS", "true"))
			.execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("CI").doesNotContain("Local");
	}

	@Test
	void whenGitHubActionsEnvVarsArePresentThenBuildScanHasACiBuildLinkToIt() {
		Map<String, String> env = new HashMap<>();
		env.put("GITHUB_ACTIONS", "true");
		env.put("GITHUB_SERVER_URL", "https://github.com");
		env.put("GITHUB_REPOSITORY", "spring-projects/spring-boot");
		env.put("GITHUB_RUN_ID", "1234567890");
		new BuildScanConventions(this.develocity, this.processRunner, env).execute(this.buildScan);
		assertThat(this.buildScan.links).containsEntry("CI build",
				"https://github.com/spring-projects/spring-boot/actions/runs/1234567890");
	}

	@Test
	void whenGitHubActionsEnvVarIsPresentThenBuildScanHasGitHubActionsAsTheCiProviderValue() {
		new BuildScanConventions(this.develocity, this.processRunner,
				Collections.singletonMap("GITHUB_ACTIONS", "true"))
			.execute(this.buildScan);
		assertThat(this.buildScan.values).containsEntry("CI provider", "GitHub Actions");
	}

	@Test
	void whenNoCiIndicatorsArePresentThenBuildScanIsTaggedWithLocalNotCi() {
		new BuildScanConventions(this.develocity, this.processRunner, Collections.emptyMap()).execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("Local").doesNotContain("CI");
	}

	@Test
	void whenNoCiIndicatorsArePresentThenBuildScanHasNoCiBuildLink() {
		new BuildScanConventions(this.develocity, this.processRunner, Collections.emptyMap()).execute(this.buildScan);
		assertThat(this.buildScan.links).doesNotContainKey("CI build");
	}

	@Test
	void whenNoCiIndicatorsArePresentThenBuildScanHasNoCiProviderValue() {
		new BuildScanConventions(this.develocity, this.processRunner, Collections.emptyMap()).execute(this.buildScan);
		assertThat(this.buildScan.values).doesNotContainKey("CI provider");
	}

	@Test
	void buildScanIsTaggedWithJdkVersion() {
		new BuildScanConventions(this.develocity, this.processRunner).execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("JDK-" + System.getProperty("java.specification.version"));
	}

	@Test
	void buildScanIsTaggedWithOperatingSystem() {
		new BuildScanConventions(this.develocity, this.processRunner).execute(this.buildScan);
		assertThat(this.buildScan.tags).contains(System.getProperty("os.name"));
	}

	@Test
	void whenBranchEnvVarIsPresentThenBuildScanIsTaggedAndConfiguredWithCustomValue() {
		new BuildScanConventions(this.develocity, this.processRunner, Collections.singletonMap("BRANCH", "1.1.x"))
			.execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("1.1.x");
		assertThat(this.buildScan.values).containsEntry("Git branch", "1.1.x");
	}

	@Test
	void whenBranchEnvVarIsNotPresentThenBuildScanIsTaggedWithBranchFromGit() {
		this.processRunner.commandLineOutput.put(Arrays.asList("git", "rev-parse", "--abbrev-ref", "HEAD"), "1.2.x");
		new BuildScanConventions(this.develocity, this.processRunner).execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("1.2.x");
		assertThat(this.buildScan.values).containsEntry("Git branch", "1.2.x");
	}

	@Test
	void buildScanHasGitCommitIdCustomValueAndLinkToBuildScansForTheSameCommit() {
		this.processRunner.commandLineOutput.put(Arrays.asList("git", "rev-parse", "--short=8", "--verify", "HEAD"),
				"79ce52f8");
		new BuildScanConventions(this.develocity, this.processRunner).execute(this.buildScan);
		assertThat(this.buildScan.values).containsEntry("Git commit", "79ce52f8");
		assertThat(this.buildScan.links).containsEntry("Git commit build scans",
				"https://ge.spring.io/scans?search.names=Git+commit&search.values=79ce52f8");
	}

	@Test
	void whenGitStatusIsCleanThenBuildScanIsNotTaggedDirtyAndHasNotGitStatusCustomValue() {
		this.processRunner.commandLineOutput.put(Arrays.asList("git", "status", "--porcelain"), "");
		new BuildScanConventions(this.develocity, this.processRunner).execute(this.buildScan);
		assertThat(this.buildScan.tags).doesNotContain("dirty");
		assertThat(this.buildScan.values).doesNotContainKey("Git status");
	}

	@Test
	void whenGitStatusIsDirtyThenBuildScanIsTaggedDirtyAndHasGitStatusCustomValue() {
		this.processRunner.commandLineOutput.put(Arrays.asList("git", "status", "--porcelain"), " M build.gradle");
		new BuildScanConventions(this.develocity, this.processRunner).execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("dirty");
		assertThat(this.buildScan.values).containsEntry("Git status", "M build.gradle");
	}

	@Test
	void whenBuildingLocallyThenBackgroundUploadIsEnabled() {
		new BuildScanConventions(this.develocity, this.processRunner, Collections.emptyMap()).execute(this.buildScan);
		assertThat(this.buildScan.uploadInBackground.get()).isTrue();
	}

	@Test
	void whenBuildingOnCiThenBackgroundUploadIsDisabled() {
		new BuildScanConventions(this.develocity, this.processRunner, Collections.singletonMap("CI", null))
			.execute(this.buildScan);
		assertThat(this.buildScan.uploadInBackground.get()).isFalse();
	}

}
