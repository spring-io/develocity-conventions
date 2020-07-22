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

package io.spring.ge.conventions.maven;

import java.net.InetAddress;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.gradle.maven.extension.api.scan.BuildScanApi;
import com.gradle.maven.extension.api.scan.BuildScanDataObfuscation;
import io.spring.ge.conventions.core.ConfigurableBuildScan;

/**
 * A {@link ConfigurableBuildScan} that configures a {@link BuildScanApi build scan} for a
 * Maven build.
 *
 * @author Andy Wilkinson
 */
class MavenConfigurableBuildScan implements ConfigurableBuildScan {

	private final BuildScanApi buildScan;

	MavenConfigurableBuildScan(BuildScanApi buildScan) {
		this.buildScan = buildScan;
	}

	@Override
	public void captureInputFiles(boolean capture) {
		this.buildScan.setCaptureGoalInputFiles(capture);
	}

	@Override
	public String server() {
		return this.buildScan.getServer();
	}

	@Override
	public void server(String server) {
		this.buildScan.setServer(server);
	}

	@Override
	public void uploadInBackground(boolean enabled) {
		this.buildScan.setUploadInBackground(enabled);
	}

	@Override
	public void obfuscation(Consumer<ObfuscationConfigurer> configurer) {
		configurer.accept(new MavenObfusactionConfigurer(this.buildScan.getObfuscation()));
	}

	@Override
	public void publishAlways() {
		this.buildScan.publishAlways();
	}

	@Override
	public void publishIfAuthenticated() {
		// Enabled via gradle-enterprise.xml
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
	public void background(Consumer<ConfigurableBuildScan> buildScan) {
		this.buildScan.background((api) -> buildScan.accept(new MavenConfigurableBuildScan(api)));
	}

	private static final class MavenObfusactionConfigurer implements ObfuscationConfigurer {

		private final BuildScanDataObfuscation obfuscation;

		private MavenObfusactionConfigurer(BuildScanDataObfuscation obfuscation) {
			this.obfuscation = obfuscation;
		}

		@Override
		public void ipAddresses(Function<? super List<InetAddress>, ? extends List<String>> obfuscator) {
			this.obfuscation.ipAddresses(obfuscator);
		}

	}

}
