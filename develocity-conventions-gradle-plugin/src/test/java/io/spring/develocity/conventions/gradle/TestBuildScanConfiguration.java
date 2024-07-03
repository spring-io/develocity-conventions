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

package io.spring.develocity.conventions.gradle;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.gradle.develocity.agent.gradle.scan.BuildResult;
import com.gradle.develocity.agent.gradle.scan.BuildScanCaptureConfiguration;
import com.gradle.develocity.agent.gradle.scan.BuildScanConfiguration;
import com.gradle.develocity.agent.gradle.scan.BuildScanDataObfuscationConfiguration;
import com.gradle.develocity.agent.gradle.scan.BuildScanPublishingConfiguration;
import com.gradle.develocity.agent.gradle.scan.PublishedBuildScan;
import org.gradle.api.Action;
import org.gradle.api.provider.Property;
import org.gradle.api.specs.Spec;

/**
 * A {@link BuildScanConfiguration} used for unit testing.
 *
 * @author Andy Wilkinson
 */
class TestBuildScanConfiguration implements BuildScanConfiguration {

	final TestBuildScanDataObfuscation obfuscation = new TestBuildScanDataObfuscation();

	final List<String> tags = new ArrayList<>();

	final Map<String, String> values = new HashMap<>();

	final Map<String, String> links = new HashMap<>();

	final TestBuildScanCaptureSettings captureSettings = new TestBuildScanCaptureSettings();

	final TestBuildScanPublishingConfiguration publishing = new TestBuildScanPublishingConfiguration();

	final Property<Boolean> uploadInBackground = new TestProperty<>();

	@Override
	public void link(String name, String url) {
		this.links.put(name, url);
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
	public void background(Action<? super BuildScanConfiguration> action) {
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
	public void capture(Action<? super BuildScanCaptureConfiguration> action) {
		action.execute(this.captureSettings);
	}

	@Override
	public BuildScanCaptureConfiguration getCapture() {
		throw new UnsupportedOperationException();
	}

	@Override
	public BuildScanDataObfuscationConfiguration getObfuscation() {
		return this.obfuscation;
	}

	@Override
	public TestBuildScanPublishingConfiguration getPublishing() {
		return this.publishing;
	}

	@Override
	public Property<String> getTermsOfUseAgree() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Property<String> getTermsOfUseUrl() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Property<Boolean> getUploadInBackground() {
		return this.uploadInBackground;
	}

	@Override
	public void obfuscation(Action<? super BuildScanDataObfuscationConfiguration> action) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void publishing(Action<? super BuildScanPublishingConfiguration> action) {
		action.execute(this.publishing);
	}

	static final class TestBuildScanDataObfuscation implements BuildScanDataObfuscationConfiguration {

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

	static final class TestBuildScanCaptureSettings implements BuildScanCaptureConfiguration {

		final Property<Boolean> fileFingerprints = new TestProperty<>();

		@Override
		public Property<Boolean> getBuildLogging() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Property<Boolean> getFileFingerprints() {
			return this.fileFingerprints;
		}

		@Override
		public Property<Boolean> getTestLogging() {
			throw new UnsupportedOperationException();
		}

	}

	static final class TestBuildScanPublishingConfiguration implements BuildScanPublishingConfiguration {

		Spec<? super PublishingContext> predicate;

		@Override
		public void onlyIf(Spec<? super PublishingContext> predicate) {
			this.predicate = predicate;
		}

	}

}
