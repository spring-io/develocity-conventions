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

package io.spring.ge.maven;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.gradle.maven.extension.api.scan.BuildResult;
import com.gradle.maven.extension.api.scan.BuildScanApi;
import com.gradle.maven.extension.api.scan.BuildScanDataObfuscation;
import com.gradle.maven.extension.api.scan.PublishedBuildScan;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MavenConfigurableBuildScan}.
 *
 * @author Andy Wilkinson
 */
public class MavenConfigurableBuildScanTests {

	private final TestBuildScanApi api = new TestBuildScanApi();

	private final MavenConfigurableBuildScan buildScan = new MavenConfigurableBuildScan(this.api);

	@Test
	void capturingOfTaskInputsCanBeEnabled() {
		this.buildScan.captureInputFiles(true);
		assertThat(this.api.isCaptureGoalInputFiles()).isTrue();
	}

	@Test
	void ipAddressesCanBeObfuscated() throws UnknownHostException {
		this.buildScan.obfuscation((obfuscation) -> obfuscation.ipAddresses(
				(addresses) -> addresses.stream().map((address) -> "0.0.0.0").collect(Collectors.toList())));
		assertThat(this.api.obfuscation.ipAddressesObfuscator).isNotNull();
		List<String> obfuscatedAddresses = this.api.obfuscation.ipAddressesObfuscator
				.apply(Arrays.asList(InetAddress.getByName("10.0.0.1"), InetAddress.getByName("10.0.0.2")));
		assertThat(obfuscatedAddresses).containsExactly("0.0.0.0", "0.0.0.0");
	}

	@Test
	void publishAlwaysCanBeEnabled() {
		this.buildScan.publishAlways();
		assertThat(this.api.publishAlways).isTrue();
	}

	@Test
	void serverCanBeConfigured() {
		this.buildScan.server("https://ge.spring.io");
		assertThat(this.api.server).isEqualTo("https://ge.spring.io");
		assertThat(this.buildScan.server()).isEqualTo("https://ge.spring.io");
	}

	@Test
	void linksCanBeAddedToTheBuildScan() {
		this.buildScan.link("Example", "https://example.com");
		assertThat(this.api.links).hasSize(1);
		assertThat(this.api.links).containsEntry("Example", "https://example.com");
	}

	@Test
	void tagsCanBeAddedToTheBuildScan() {
		this.buildScan.tag("JDK-1.8");
		this.buildScan.tag("CI");
		assertThat(this.api.tags).containsExactly("JDK-1.8", "CI");
	}

	@Test
	void valuesCanBeAddedToTheBuildScan() {
		this.buildScan.value("Git commit", "abcd1234");
		assertThat(this.api.values).hasSize(1);
		assertThat(this.api.values).containsEntry("Git commit", "abcd1234");
	}

	@Test
	void uploadInBackgroundCanBeDisabled() {
		this.buildScan.uploadInBackground(false);
		assertThat(this.api.uploadInBackground).isFalse();
	}

	@Test
	void backgroundConfigurationIsPerformed() {
		this.buildScan.background((scan) -> scan.server("https://background.example.com"));
		assertThat(this.api.server).isEqualTo("https://background.example.com");
	}

	public static final class TestBuildScanApi implements BuildScanApi {

		private final TestBuildScanDataObfuscation obfuscation = new TestBuildScanDataObfuscation();

		private final List<String> tags = new ArrayList<String>();

		private final Map<String, String> values = new HashMap<>();

		private final Map<String, String> links = new HashMap<>();

		private boolean captureGoalInputFiles;

		private boolean publishAlways;

		private boolean publishIfAuthenticated;

		private String server;

		private boolean uploadInBackground = true;

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
		public void link(String name, String url) {
			this.links.put(name, url);
		}

		@Override
		public void publishAlways() {
			this.publishAlways = true;
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

		@Override
		public void background(Consumer<? super BuildScanApi> action) {
			action.accept(this);
		}

		@Override
		public void buildFinished(Consumer<? super BuildResult> action) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void buildScanPublished(Consumer<? super PublishedBuildScan> action) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void executeOnce(String identifier, Consumer<? super BuildScanApi> action) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isCaptureGoalInputFiles() {
			return this.captureGoalInputFiles;
		}

		@Override
		public void obfuscation(Consumer<? super BuildScanDataObfuscation> action) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void publishOnDemand() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setCaptureGoalInputFiles(boolean capture) {
			this.captureGoalInputFiles = capture;
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

}
