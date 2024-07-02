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

package io.spring.develocity.conventions.maven;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.gradle.develocity.agent.maven.api.scan.BuildResult;
import com.gradle.develocity.agent.maven.api.scan.BuildScanApi;
import com.gradle.develocity.agent.maven.api.scan.BuildScanCaptureSettings;
import com.gradle.develocity.agent.maven.api.scan.BuildScanDataObfuscation;
import com.gradle.develocity.agent.maven.api.scan.BuildScanPublishing;
import com.gradle.develocity.agent.maven.api.scan.BuildScanPublishing.PublishingContext;
import com.gradle.develocity.agent.maven.api.scan.PublishedBuildScan;
import io.spring.develocity.conventions.core.ConfigurableBuildScan;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link MavenConfigurableBuildScan}.
 *
 * @author Andy Wilkinson
 */
public class MavenConfigurableBuildScanTests {

	private final TestBuildScanApi buildScanApi = new TestBuildScanApi();

	private final MavenConfigurableBuildScan buildScan = new MavenConfigurableBuildScan(this.buildScanApi);

	@Test
	void captureOfInputFilesCanBeEnabled() {
		this.buildScan.captureInputFiles(true);
		assertThat(this.buildScanApi.capture.isFileFingerprints()).isTrue();
	}

	@Test
	void ipAddressesCanBeObfuscated() throws UnknownHostException {
		this.buildScan.obfuscation((obfuscation) -> obfuscation.ipAddresses(
				(inetAddresses) -> inetAddresses.stream().map((address) -> "127.0.0.1").collect(Collectors.toList())));
		assertThat(this.buildScanApi.obfuscation.ipAddressesObfuscator).isNotNull();
		List<String> obfuscatedAddresses = this.buildScanApi.obfuscation.ipAddressesObfuscator
			.apply(Arrays.asList(InetAddress.getByName("10.0.0.1"), InetAddress.getByName("10.0.0.2")));
		assertThat(obfuscatedAddresses).containsExactly("127.0.0.1", "127.0.0.1");
	}

	@Test
	void buildScansCanBeConfiguredToPublishIfAuthenticated() {
		this.buildScan.publishIfAuthenticated();
		PublishingContext context = mock(PublishingContext.class);
		given(context.isAuthenticated()).willReturn(true);
		assertThat(this.buildScanApi.publishing.onlyIf.test(context)).isTrue();
		given(context.isAuthenticated()).willReturn(false);
		assertThat(this.buildScanApi.publishing.onlyIf.test(context)).isFalse();
	}

	@Test
	void whenTagsAreAddedThenBuildScanHasTags() {
		this.buildScan.tag("some-tag");
		this.buildScan.tag("another-tag");
		assertThat(this.buildScanApi.tags).containsExactly("some-tag", "another-tag");
	}

	@Test
	void whenLinkIsAddedThenBuildScanHasLink() {
		this.buildScan.link("CI Server", "ci.example.com");
		assertThat(this.buildScanApi.links).containsEntry("CI Server", "ci.example.com");
	}

	@Test
	void whenValueIsAddedThenBuildScanHasValue() {
		this.buildScan.value("Branch", "1.2.x");
		assertThat(this.buildScanApi.values).containsEntry("Branch", "1.2.x");
	}

	@Test
	void whenBuildingLocallyThenBackgroundUploadIsEnabled() {
		this.buildScan.uploadInBackground(true);
		assertThat(this.buildScanApi.uploadInBackground).isTrue();
	}

	@Test
	void backgroundTasksAreInvoked() {
		AtomicBoolean invoked = new AtomicBoolean();
		Consumer<ConfigurableBuildScan> consumer = (buildScan) -> invoked.set(true);
		this.buildScan.background(consumer);
		assertThat(invoked.get()).isTrue();
	}

	private static final class TestBuildScanApi implements BuildScanApi {

		private final TestBuildScanCaptureSettings capture = new TestBuildScanCaptureSettings();

		private final TestBuildScanDataObfuscation obfuscation = new TestBuildScanDataObfuscation();

		private final TestBuildScanPublishing publishing = new TestBuildScanPublishing();

		private final List<String> tags = new ArrayList<>();

		private final Map<String, String> links = new HashMap<>();

		private final Map<String, String> values = new HashMap<>();

		private boolean uploadInBackground;

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
		public void capture(Consumer<? super BuildScanCaptureSettings> action) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void executeOnce(String identifier, Consumer<? super BuildScanApi> action) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean getAllowUntrustedServer() {
			throw new UnsupportedOperationException();
		}

		@Override
		public BuildScanCaptureSettings getCapture() {
			return this.capture;
		}

		@Override
		public BuildScanDataObfuscation getObfuscation() {
			return this.obfuscation;
		}

		@Override
		public BuildScanPublishing getPublishing() {
			return this.publishing;
		}

		@Override
		public String getServer() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getTermsOfUseAgree() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getTermsOfUseUrl() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isUploadInBackground() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void link(String name, String url) {
			this.links.put(name, url);
		}

		@Override
		public void obfuscation(Consumer<? super BuildScanDataObfuscation> action) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void publishing(Consumer<? super BuildScanPublishing> action) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setAllowUntrustedServer(boolean allow) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setServer(URI url) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setTermsOfUseAgree(String agree) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setTermsOfUseUrl(String termsOfUseUrl) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setUploadInBackground(boolean uploadInBackground) {
			this.uploadInBackground = uploadInBackground;
		}

		@Override
		public void tag(String tag) {
			this.tags.add(tag);
		}

		@Override
		public void value(String name, String value) {
			this.values.put(name, value);
		}

		static class TestBuildScanCaptureSettings implements BuildScanCaptureSettings {

			private boolean fileFingerprints;

			@Override
			public boolean isBuildLogging() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void setBuildLogging(boolean capture) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isFileFingerprints() {
				return this.fileFingerprints;
			}

			@Override
			public boolean isTestLogging() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void setTestLogging(boolean capture) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void setFileFingerprints(boolean capture) {
				this.fileFingerprints = capture;
			}

		}

		static final class TestBuildScanDataObfuscation implements BuildScanDataObfuscation {

			Function<? super List<InetAddress>, ? extends List<String>> ipAddressesObfuscator;

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

		static final class TestBuildScanPublishing implements BuildScanPublishing {

			private Predicate<PublishingContext> onlyIf;

			@Override
			public BuildScanPublishing onlyIf(Predicate<PublishingContext> onlyIf) {
				this.onlyIf = onlyIf;
				return this;
			}

		}

	}

}
