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

package io.spring.ge.conventions.gradle;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.gradle.enterprise.gradleplugin.internal.extension.BuildScanExtensionWithHiddenFeatures;
import com.gradle.scan.plugin.BuildResult;
import com.gradle.scan.plugin.BuildScanDataObfuscation;
import com.gradle.scan.plugin.BuildScanExtension;
import com.gradle.scan.plugin.PublishedBuildScan;
import org.gradle.api.Action;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link GradleConfigurableBuildScan}.
 *
 * @author Andy Wilkinson
 */
class GradleConfigurableBuildScanTests {

	private final TestBuildScanExtension extension = new TestBuildScanExtension();

	private final GradleConfigurableBuildScan buildScan = new GradleConfigurableBuildScan(this.extension);

	@Test
	void capturingOfTaskInputsCanBeEnabled() {
		this.buildScan.captureInputFiles(true);
		assertThat(this.extension.isCaptureTaskInputFiles()).isTrue();
	}

	@Test
	void ipAddressesCanBeObfuscated() throws UnknownHostException {
		this.buildScan.obfuscation((obfuscation) -> obfuscation.ipAddresses(
				(addresses) -> addresses.stream().map((address) -> "0.0.0.0").collect(Collectors.toList())));
		assertThat(this.extension.obfuscation.ipAddressesObfuscator).isNotNull();
		List<String> obfuscatedAddresses = this.extension.obfuscation.ipAddressesObfuscator
				.apply(Arrays.asList(InetAddress.getByName("10.0.0.1"), InetAddress.getByName("10.0.0.2")));
		assertThat(obfuscatedAddresses).containsExactly("0.0.0.0", "0.0.0.0");
	}

	@Test
	void publishAlwaysCanBeEnabled() {
		this.buildScan.publishAlways();
		assertThat(this.extension.publishAlways).isTrue();
	}

	@Test
	void publishIfAuthenticatedCanBeEnabled() {
		this.buildScan.publishIfAuthenticated();
		assertThat(this.extension.publishIfAuthenticated).isTrue();
	}

	@Test
	void serverCanBeConfigured() {
		this.buildScan.server("https://ge.spring.io");
		assertThat(this.extension.server).isEqualTo("https://ge.spring.io");
		assertThat(this.buildScan.server()).isEqualTo("https://ge.spring.io");
	}

	@Test
	void linksCanBeAddedToTheBuildScan() {
		this.buildScan.link("Example", "https://example.com");
		assertThat(this.extension.links).hasSize(1);
		assertThat(this.extension.links).containsEntry("Example", "https://example.com");
	}

	@Test
	void tagsCanBeAddedToTheBuildScan() {
		this.buildScan.tag("JDK-1.8");
		this.buildScan.tag("CI");
		assertThat(this.extension.tags).containsExactly("JDK-1.8", "CI");
	}

	@Test
	void valuesCanBeAddedToTheBuildScan() {
		this.buildScan.value("Git commit", "abcd1234");
		assertThat(this.extension.values).hasSize(1);
		assertThat(this.extension.values).containsEntry("Git commit", "abcd1234");
	}

	@Test
	void uploadInBackgroundCanBeDisabled() {
		this.buildScan.uploadInBackground(false);
		assertThat(this.extension.uploadInBackground).isFalse();
	}

	@Test
	void backgroundConfigurationIsPerformed() {
		this.buildScan.background((scan) -> scan.server("https://background.example.com"));
		assertThat(this.extension.server).isEqualTo("https://background.example.com");
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

}
