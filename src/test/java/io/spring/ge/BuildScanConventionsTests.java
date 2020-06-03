/*
 * Copyright 2020 the original author or authors.
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

package io.spring.ge;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.gradle.scan.plugin.BuildResult;
import com.gradle.scan.plugin.BuildScanDataObfuscation;
import com.gradle.scan.plugin.BuildScanExtension;
import com.gradle.scan.plugin.PublishedBuildScan;
import com.gradle.scan.plugin.internal.api.BuildScanExtensionWithHiddenFeatures;
import org.gradle.api.Action;
import org.gradle.api.internal.ProcessOperations;
import org.gradle.process.ExecResult;
import org.gradle.process.ExecSpec;
import org.gradle.process.JavaExecSpec;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link BuildScanConventions}.
 *
 * @author Andy Wilkinson
 */
class BuildScanConventionsTests {

	private final TestProcessOperations processOperations = new TestProcessOperations();

	private final TestBuildScanExtension buildScan = new TestBuildScanExtension();

	@Test
	void capturingOfTaskInputsIsEnabled() {
		new BuildScanConventions(this.processOperations).execute(this.buildScan);
		assertThat(this.buildScan.isCaptureTaskInputFiles()).isTrue();
	}

	@Test
	void ipAddressesAreObfuscated() throws UnknownHostException {
		new BuildScanConventions(this.processOperations).execute(this.buildScan);
		assertThat(this.buildScan.obfuscation.ipAddressesObfuscator).isNotNull();
		List<String> obfuscatedAddresses = this.buildScan.obfuscation.ipAddressesObfuscator
				.apply(Arrays.asList(InetAddress.getByName("10.0.0.1"), InetAddress.getByName("10.0.0.2")));
		assertThat(obfuscatedAddresses).containsExactly("0.0.0.0", "0.0.0.0");
	}

	@Test
	void buildScansAreConfiguredToAlwaysPublishWhenAuthenticated() {
		new BuildScanConventions(this.processOperations).execute(this.buildScan);
		assertThat(this.buildScan.publishAlways).isTrue();
		assertThat(this.buildScan.publishIfAuthenticated).isTrue();
	}

	@Test
	void buildScansAreConfiguredToPublishToGeSpringIo() {
		new BuildScanConventions(this.processOperations).execute(this.buildScan);
		assertThat(this.buildScan.server).isEqualTo("https://ge.spring.io");
	}

	@Test
	void whenBambooResultEnvVarIsPresentThenBuildScanIsTaggedWithCiNotLocal() {
		new BuildScanConventions(this.processOperations,
				Collections.singletonMap("bamboo_resultsUrl", "https://bamboo.exampl.com")).execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("CI").doesNotContain("Local");
	}

	@Test
	void whenBambooResultEnvVarIsPresentThenBuildScanHasACiBuildLinkToIt() {
		new BuildScanConventions(this.processOperations,
				Collections.singletonMap("bamboo_resultsUrl", "https://bamboo.example.com")).execute(this.buildScan);
		assertThat(this.buildScan.links).containsEntry("CI build", "https://bamboo.example.com");
	}

	@Test
	void whenJenkinsUrlAndBuildUrlEnvVarsArePresentThenBuildScanHasACiBuildLinkToBuildUrl() {
		Map<String, String> env = new HashMap<>();
		env.put("JENKINS_URL", "https://jenkins.example.com");
		env.put("BUILD_URL", "https://jenkins.example.com/builds/123");
		new BuildScanConventions(this.processOperations, env).execute(this.buildScan);
		assertThat(this.buildScan.links).containsEntry("CI build", "https://jenkins.example.com/builds/123");
	}

	@Test
	void whenCiEnvVarIsPresentThenBuildScanIsTaggedWithCiNotLocal() {
		new BuildScanConventions(this.processOperations, Collections.singletonMap("CI", null)).execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("CI").doesNotContain("Local");
	}

	@Test
	void whenJenkinsUrlEnvVarIsPresentThenBuildScanIsTaggedWithCiNotLocal() {
		new BuildScanConventions(this.processOperations,
				Collections.singletonMap("JENKINS_URL", "https://jenkins.example.com")).execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("CI").doesNotContain("Local");
	}

	@Test
	void whenNoCiIndicatorsArePresentThenBuildScanIsTaggedWithLocalNotCi() {
		new BuildScanConventions(this.processOperations).execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("Local").doesNotContain("CI");
	}

	@Test
	void buildScanIsTaggedWithJdkVersion() {
		new BuildScanConventions(this.processOperations).execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("JDK-" + System.getProperty("java.specification.version"));
	}

	@Test
	void buildScanIsTaggedWithOperatingSystem() {
		new BuildScanConventions(this.processOperations).execute(this.buildScan);
		assertThat(this.buildScan.tags).contains(System.getProperty("os.name"));
	}

	@Test
	void whenBranchEnvVarIsPresentThenBuildScanIsTaggedAndConfiguredWithCustomValue() {
		new BuildScanConventions(this.processOperations, Collections.singletonMap("BRANCH", "1.1.x"))
				.execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("1.1.x");
		assertThat(this.buildScan.values).containsEntry("Git branch", "1.1.x");
	}

	@Test
	void whenBranchEnvVarIsNotPresentThenBuildScanIsTaggedWithBranchFromGit() {
		this.processOperations.commandLineOutput.put(Arrays.asList("git", "rev-parse", "--abbrev-ref", "HEAD"),
				"1.2.x");
		new BuildScanConventions(this.processOperations).execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("1.2.x");
		assertThat(this.buildScan.values).containsEntry("Git branch", "1.2.x");
	}

	@Test
	void buildScanHasGitCommitIdCustomValueAndLinkToBuildScansForTheSameCommit() {
		this.processOperations.commandLineOutput.put(Arrays.asList("git", "rev-parse", "--short=8", "--verify", "HEAD"),
				"79ce52f8");
		new BuildScanConventions(this.processOperations).execute(this.buildScan);
		assertThat(this.buildScan.values).containsEntry("Git commit", "79ce52f8");
		assertThat(this.buildScan.links).containsEntry("Git commit build scans",
				"https://ge.spring.io/scans?search.names=Git+commit&search.values=79ce52f8");
	}

	@Test
	void whenGitStatusIsCleanThenBuildScanIsNotTaggedDirtyAndHasNotGitStatusCustomValue() {
		this.processOperations.commandLineOutput.put(Arrays.asList("git", "status", "--porcelain"), "");
		new BuildScanConventions(this.processOperations).execute(this.buildScan);
		assertThat(this.buildScan.tags).doesNotContain("dirty");
		assertThat(this.buildScan.values).doesNotContainKey("Git status");
	}

	@Test
	void whenGitStatusIsDirtyThenBuildScanIsTaggedDirtyAndHasGitStatusCustomValue() {
		this.processOperations.commandLineOutput.put(Arrays.asList("git", "status", "--porcelain"), " M build.gradle");
		new BuildScanConventions(this.processOperations).execute(this.buildScan);
		assertThat(this.buildScan.tags).contains("dirty");
		assertThat(this.buildScan.values).containsEntry("Git status", "M build.gradle");
	}

	@Test
	void whenBuildingLocallyThenBackgroundUploadIsEnabled() {
		new BuildScanConventions(this.processOperations).execute(this.buildScan);
		assertThat(this.buildScan.uploadInBackground).isTrue();
	}

	@Test
	void whenBuildingOnCiThenBackgroundUploadIsDisabled() {
		new BuildScanConventions(this.processOperations, Collections.singletonMap("CI", null)).execute(this.buildScan);
		assertThat(this.buildScan.uploadInBackground).isFalse();
	}

	public static final class TestBuildScanExtension implements BuildScanExtensionWithHiddenFeatures {

		private final TestBuildScanDataObfuscation obfuscation = new TestBuildScanDataObfuscation();

		private final List<String> tags = new ArrayList<String>();

		private final Map<String, String> values = new HashMap<>();

		private final Map<String, String> links = new HashMap<>();

		private boolean captureTaskInputFiles;

		private boolean publishAlways;

		private boolean publishIfAuthenticated;

		private String server;

		private boolean uploadInBackground = true;

		@Override
		public void background(Action<? super BuildScanExtension> action) {
			action.execute(this);
		}

		@Override
		public void buildFinished(Action<? super BuildResult> action) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void buildScanPublished(Action<? super PublishedBuildScan> action) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean getAllowUntrustedServer() {
			throw new UnsupportedOperationException();
		}

		@Override
		public BuildScanDataObfuscation getObfuscation() {
			return this.obfuscation;
		}

		@Override
		public String getServer() {
			return this.server;
		}

		@Override
		public String getTermsOfServiceAgree() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getTermsOfServiceUrl() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isCaptureTaskInputFiles() {
			return this.captureTaskInputFiles;
		}

		@Override
		public void link(String name, String url) {
			this.links.put(name, url);
		}

		@Override
		public void obfuscation(Action<? super BuildScanDataObfuscation> action) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void onError(Action<String> action) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void publishAlways() {
			this.publishAlways = true;
		}

		@Override
		public void publishIfAuthenticated() {
			this.publishIfAuthenticated = true;
		}

		@Override
		public void publishAlwaysIf(boolean condition) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void publishOnFailure() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void publishOnFailureIf(boolean condition) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setAllowUntrustedServer(boolean allow) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setCaptureTaskInputFiles(boolean capture) {
			this.captureTaskInputFiles = capture;
		}

		@Override
		public void setServer(String server) {
			this.server = server;
		}

		@Override
		public void setTermsOfServiceAgree(String agree) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setTermsOfServiceUrl(String termsOfServiceUrl) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void tag(String tag) {
			this.tags.add(tag);
		}

		@Override
		public void value(String name, String value) {
			this.values.put(name, value);
		}

		@Override
		public boolean isUploadInBackground() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setUploadInBackground(boolean uploadInBackground) {
			this.uploadInBackground = uploadInBackground;
		}

	}

	private static final class TestBuildScanDataObfuscation implements BuildScanDataObfuscation {

		private Function<? super List<InetAddress>, ? extends List<String>> ipAddressesObfuscator;

		@Override
		public void hostname(Function<? super String, ? extends String> obfuscator) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void ipAddresses(Function<? super List<InetAddress>, ? extends List<String>> obfuscator) {
			this.ipAddressesObfuscator = obfuscator;
		}

		@Override
		public void username(Function<? super String, ? extends String> obfuscator) {
			throw new UnsupportedOperationException();
		}

	}

	private static final class TestProcessOperations implements ProcessOperations {

		private final Map<List<String>, String> commandLineOutput = new HashMap<>();

		@Override
		public ExecResult exec(Action<? super ExecSpec> action) {
			ExecSpec execSpec = mock(ExecSpec.class);
			action.execute(execSpec);
			ArgumentCaptor<Object> commandLineCaptor = ArgumentCaptor.forClass(Object.class);
			verify(execSpec).setCommandLine(commandLineCaptor.capture());
			ArgumentCaptor<OutputStream> standardOut = ArgumentCaptor.forClass(OutputStream.class);
			verify(execSpec).setStandardOutput(standardOut.capture());
			List<Object> commandLine = commandLineCaptor.getAllValues();
			String output = this.commandLineOutput.get(commandLine);
			if (output != null) {
				try {
					standardOut.getValue().write(output.getBytes());
				}
				catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
			return null;
		}

		@Override
		public ExecResult javaexec(Action<? super JavaExecSpec> spec) {
			throw new UnsupportedOperationException();
		}

	}

}
