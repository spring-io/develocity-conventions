/*
 * Copyright 2020-2021 the original author or authors.
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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BuildScanConventions}.
 *
 * @author Andy Wilkinson
 */
class BuildScanConventionsTests {

	private final TestProcessRunner processRunner = new TestProcessRunner();

	private final TestBuildScanExtension buildScan = new TestBuildScanExtension();

	@Test
	void capturingOfTaskInputsIsEnabled() {
		new BuildScanConventions(this.processRunner).execute(this.buildScan);
		assertThat(this.buildScan.captureTaskInputFiles).isTrue();
	}

	@Test
	void ipAddressesAreObfuscated() throws UnknownHostException {
		new BuildScanConventions(this.processRunner).execute(this.buildScan);
		assertThat(this.buildScan.obfuscation.ipAddressesObfuscator).isNotNull();
		List<String> obfuscatedAddresses = this.buildScan.obfuscation.ipAddressesObfuscator
				.apply(Arrays.asList(InetAddress.getByName("10.0.0.1"), InetAddress.getByName("10.0.0.2")));
		assertThat(obfuscatedAddresses).containsExactly("0.0.0.0", "0.0.0.0");
	}

	@Test
	void buildScansAreConfiguredToAlwaysPublishWhenAuthenticated() {
		new BuildScanConventions(this.processRunner).execute(this.buildScan);
		assertThat(this.buildScan.publishAlways).isTrue();
		assertThat(this.buildScan.publishIfAuthenticated).isTrue();
	}

	@Test
	void buildScansAreConfiguredToPublishToGeSpringIo() {
		new BuildScanConventions(this.processRunner).execute(this.buildScan);
		assertThat(this.buildScan.server).isEqualTo("https://ge.spring.io");
	}

	@Test
	void whenBambooResultEnvVarIsPresentThenBuildScanIsTaggedWithCiNotLocal() {
		new BuildScanConventions(this.processRunner,
				Collections.singletonMap("bamboo_resultsUrl", "https://bamboo.exampl.com")).execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("CI").doesNotContain("Local");
	}

	@Test
	void whenBambooResultEnvVarIsPresentThenBuildScanHasACiBuildLinkToIt() {
		new BuildScanConventions(this.processRunner,
				Collections.singletonMap("bamboo_resultsUrl", "https://bamboo.example.com")).execute(this.buildScan);
		assertThat(this.buildScan.links).containsEntry("CI build", "https://bamboo.example.com");
	}

	@Test
	void whenCircleBuildUrlEnvVarIsPresentThenBuildScanHasACiBuildLinkToIt() {
		new BuildScanConventions(this.processRunner,
				Collections.singletonMap("CIRCLE_BUILD_URL", "https://circleci.example.com/gh/org/project/123"))
						.execute(this.buildScan);
		assertThat(this.buildScan.links).containsEntry("CI build", "https://circleci.example.com/gh/org/project/123");
	}

	@Test
	void whenJenkinsUrlAndBuildUrlEnvVarsArePresentThenBuildScanHasACiBuildLinkToBuildUrl() {
		Map<String, String> env = new HashMap<>();
		env.put("JENKINS_URL", "https://jenkins.example.com");
		env.put("BUILD_URL", "https://jenkins.example.com/builds/123");
		new BuildScanConventions(this.processRunner, env).execute(this.buildScan);
		assertThat(this.buildScan.links).containsEntry("CI build", "https://jenkins.example.com/builds/123");
	}

	@Test
	void whenCiEnvVarIsPresentThenBuildScanIsTaggedWithCiNotLocal() {
		new BuildScanConventions(this.processRunner, Collections.singletonMap("CI", null)).execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("CI").doesNotContain("Local");
	}

	@Test
	void whenJenkinsUrlEnvVarIsPresentThenBuildScanIsTaggedWithCiNotLocal() {
		new BuildScanConventions(this.processRunner,
				Collections.singletonMap("JENKINS_URL", "https://jenkins.example.com")).execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("CI").doesNotContain("Local");
	}

	@Test
	void whenNoCiIndicatorsArePresentThenBuildScanIsTaggedWithLocalNotCi() {
		new BuildScanConventions(this.processRunner).execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("Local").doesNotContain("CI");
	}

	@Test
	void buildScanIsTaggedWithJdkVersion() {
		new BuildScanConventions(this.processRunner).execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("JDK-" + System.getProperty("java.specification.version"));
	}

	@Test
	void buildScanIsTaggedWithOperatingSystem() {
		new BuildScanConventions(this.processRunner).execute(this.buildScan);
		assertThat(this.buildScan.tags).contains(System.getProperty("os.name"));
	}

	@Test
	void whenBranchEnvVarIsPresentThenBuildScanIsTaggedAndConfiguredWithCustomValue() {
		new BuildScanConventions(this.processRunner, Collections.singletonMap("BRANCH", "1.1.x"))
				.execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("1.1.x");
		assertThat(this.buildScan.values).containsEntry("Git branch", "1.1.x");
	}

	@Test
	void whenBranchEnvVarIsNotPresentThenBuildScanIsTaggedWithBranchFromGit() {
		this.processRunner.commandLineOutput.put(Arrays.asList("git", "rev-parse", "--abbrev-ref", "HEAD"), "1.2.x");
		new BuildScanConventions(this.processRunner).execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("1.2.x");
		assertThat(this.buildScan.values).containsEntry("Git branch", "1.2.x");
	}

	@Test
	void buildScanHasGitCommitIdCustomValueAndLinkToBuildScansForTheSameCommit() {
		this.processRunner.commandLineOutput.put(Arrays.asList("git", "rev-parse", "--short=8", "--verify", "HEAD"),
				"79ce52f8");
		new BuildScanConventions(this.processRunner).execute(this.buildScan);
		assertThat(this.buildScan.values).containsEntry("Git commit", "79ce52f8");
		assertThat(this.buildScan.links).containsEntry("Git commit build scans",
				"https://ge.spring.io/scans?search.names=Git+commit&search.values=79ce52f8");
	}

	@Test
	void whenGitStatusIsCleanThenBuildScanIsNotTaggedDirtyAndHasNotGitStatusCustomValue() {
		this.processRunner.commandLineOutput.put(Arrays.asList("git", "status", "--porcelain"), "");
		new BuildScanConventions(this.processRunner).execute(this.buildScan);
		assertThat(this.buildScan.tags).doesNotContain("dirty");
		assertThat(this.buildScan.values).doesNotContainKey("Git status");
	}

	@Test
	void whenGitStatusIsDirtyThenBuildScanIsTaggedDirtyAndHasGitStatusCustomValue() {
		this.processRunner.commandLineOutput.put(Arrays.asList("git", "status", "--porcelain"), " M build.gradle");
		new BuildScanConventions(this.processRunner).execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("dirty");
		assertThat(this.buildScan.values).containsEntry("Git status", "M build.gradle");
	}

	@Test
	void whenBuildingLocallyThenBackgroundUploadIsEnabled() {
		new BuildScanConventions(this.processRunner).execute(this.buildScan);
		assertThat(this.buildScan.uploadInBackground).isTrue();
	}

	@Test
	void whenBuildingOnCiThenBackgroundUploadIsDisabled() {
		new BuildScanConventions(this.processRunner, Collections.singletonMap("CI", null)).execute(this.buildScan);
		assertThat(this.buildScan.uploadInBackground).isFalse();
	}

}
