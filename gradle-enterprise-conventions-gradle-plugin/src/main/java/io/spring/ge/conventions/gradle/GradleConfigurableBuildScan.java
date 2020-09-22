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
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.gradle.enterprise.gradleplugin.internal.extension.BuildScanExtensionWithHiddenFeatures;
import com.gradle.scan.plugin.BuildScanDataObfuscation;
import com.gradle.scan.plugin.BuildScanExtension;
import io.spring.ge.conventions.core.ConfigurableBuildScan;

/**
 * A {@link ConfigurableBuildScan} that configures a {@link BuildScanExtension} for a
 * Gradle build.
 *
 * @author Andy Wilkinson
 */
class GradleConfigurableBuildScan implements ConfigurableBuildScan {

	private final BuildScanExtension buildScan;

	GradleConfigurableBuildScan(BuildScanExtension buildScan) {
		this.buildScan = buildScan;
	}

	@Override
	public void captureInputFiles(boolean capture) {
		this.buildScan.setCaptureTaskInputFiles(capture);
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
		try {
			this.buildScan.setUploadInBackground(enabled);
		}
		catch (NoSuchMethodError ex) {
			// GE Plugin version < 3.3. Continue
		}
	}

	@Override
	public void obfuscation(Consumer<ObfuscationConfigurer> configurer) {
		configurer.accept(new GradleObfusactionConfigurer(this.buildScan.getObfuscation()));
	}

	@Override
	public void publishAlways() {
		this.buildScan.publishAlways();
	}

	@Override
	public void publishIfAuthenticated() {
		((BuildScanExtensionWithHiddenFeatures) this.buildScan).publishIfAuthenticated();
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
		this.buildScan.background((extension) -> buildScan.accept(new GradleConfigurableBuildScan(extension)));
	}

	private static final class GradleObfusactionConfigurer implements ObfuscationConfigurer {

		private final BuildScanDataObfuscation obfuscation;

		private GradleObfusactionConfigurer(BuildScanDataObfuscation obfuscation) {
			this.obfuscation = obfuscation;
		}

		@Override
		public void ipAddresses(Function<? super List<InetAddress>, ? extends List<String>> obfuscator) {
			this.obfuscation.ipAddresses(obfuscator);
		}

	}

}
