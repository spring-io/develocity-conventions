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
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.gradle.develocity.agent.gradle.scan.BuildScanConfiguration;
import com.gradle.develocity.agent.gradle.scan.BuildScanDataObfuscationConfiguration;
import com.gradle.develocity.agent.gradle.scan.BuildScanPublishingConfiguration.PublishingContext;
import io.spring.ge.conventions.core.ConfigurableBuildScan;

/**
 * A {@link ConfigurableBuildScan} for Gradle builds.
 *
 * @author Andy Wilkinson
 */
class GradleConfigurableBuildScan implements ConfigurableBuildScan {

	private final BuildScanConfiguration buildScan;

	GradleConfigurableBuildScan(BuildScanConfiguration buildScan) {
		this.buildScan = buildScan;
	}

	@Override
	public void captureInputFiles(boolean capture) {
		this.buildScan.capture((settings) -> settings.getFileFingerprints().set(capture));
	}

	@Override
	public void obfuscation(Consumer<ObfuscationConfigurer> configurer) {
		configurer.accept(new GradleObfuscationConfigurer(this.buildScan.getObfuscation()));
	}

	@Override
	public void publishIfAuthenticated() {
		this.buildScan.publishing((publishing) -> publishing.onlyIf(PublishingContext::isAuthenticated));
	}

	@Override
	public void uploadInBackground(boolean enabled) {
		this.buildScan.getUploadInBackground().set(enabled);
	}

	@Override
	public void link(String name, String url) {
		this.buildScan.link(name, url);
	}

	@Override
	public void tag(String tag) {
		this.buildScan.tag(tag);
	}

	@Override
	public void value(String name, String value) {
		this.buildScan.value(name, value);
	}

	@Override
	public void background(Consumer<ConfigurableBuildScan> backgroundConfigurer) {
		this.buildScan
			.background((buildScan) -> backgroundConfigurer.accept(new GradleConfigurableBuildScan(buildScan)));
	}

	private static final class GradleObfuscationConfigurer implements ObfuscationConfigurer {

		private final BuildScanDataObfuscationConfiguration obfuscation;

		private GradleObfuscationConfigurer(BuildScanDataObfuscationConfiguration obfuscation) {
			this.obfuscation = obfuscation;
		}

		@Override
		public void ipAddresses(Function<? super List<InetAddress>, ? extends List<String>> obfuscator) {
			this.obfuscation.ipAddresses(obfuscator);
		}

	}

}
