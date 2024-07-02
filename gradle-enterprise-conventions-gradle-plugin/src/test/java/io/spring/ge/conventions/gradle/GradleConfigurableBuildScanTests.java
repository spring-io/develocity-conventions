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
import java.util.List;
import java.util.stream.Collectors;

import com.gradle.develocity.agent.gradle.scan.BuildScanPublishingConfiguration.PublishingContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link GradleConfigurableBuildScan}.
 *
 * @author Andy Wilkinson
 */
class GradleConfigurableBuildScanTests {

	private final TestBuildScanConfiguration buildScan = new TestBuildScanConfiguration();

	@Test
	void captureOfInputFilesCanBeEnabled() {
		new GradleConfigurableBuildScan(this.buildScan).captureInputFiles(true);
		assertThat(this.buildScan.captureSettings.getFileFingerprints().get()).isTrue();
	}

	@Test
	void ipAddressesCanBeObfuscated() throws UnknownHostException {
		new GradleConfigurableBuildScan(this.buildScan).obfuscation((obfuscation) -> obfuscation.ipAddresses(
				(inetAddresses) -> inetAddresses.stream().map((address) -> "127.0.0.1").collect(Collectors.toList())));
		assertThat(this.buildScan.obfuscation.ipAddressesObfuscator).isNotNull();
		List<String> obfuscatedAddresses = this.buildScan.obfuscation.ipAddressesObfuscator
			.apply(Arrays.asList(InetAddress.getByName("10.0.0.1"), InetAddress.getByName("10.0.0.2")));
		assertThat(obfuscatedAddresses).containsExactly("127.0.0.1", "127.0.0.1");
	}

	@Test
	void buildScansCanBeConfiguredToPublishIfAuthenticated() {
		new GradleConfigurableBuildScan(this.buildScan).publishIfAuthenticated();
		PublishingContext context = mock(PublishingContext.class);
		given(context.isAuthenticated()).willReturn(true);
		assertThat(this.buildScan.publishing.predicate.isSatisfiedBy(context)).isTrue();
		given(context.isAuthenticated()).willReturn(false);
		assertThat(this.buildScan.publishing.predicate.isSatisfiedBy(context)).isFalse();
	}

	@Test
	void whenTagsAreAddedThenBuildScanHasTags() {
		GradleConfigurableBuildScan configurableBuildScan = new GradleConfigurableBuildScan(this.buildScan);
		configurableBuildScan.tag("some-tag");
		configurableBuildScan.tag("another-tag");
		assertThat(this.buildScan.tags).containsExactly("some-tag", "another-tag");
	}

	@Test
	void whenLinkIsAddedThenBuildScanHasLink() {
		new GradleConfigurableBuildScan(this.buildScan).link("CI Server", "ci.example.com");
		assertThat(this.buildScan.links).containsEntry("CI Server", "ci.example.com");
	}

	@Test
	void whenValueIsAddedThenBuildScanHasValue() {
		new GradleConfigurableBuildScan(this.buildScan).value("Branch", "1.2.x");
		assertThat(this.buildScan.values).containsEntry("Branch", "1.2.x");
	}

	@Test
	void whenBuildingLocallyThenBackgroundUploadIsEnabled() {
		new GradleConfigurableBuildScan(this.buildScan).uploadInBackground(true);
		assertThat(this.buildScan.uploadInBackground.get()).isTrue();
	}

}
