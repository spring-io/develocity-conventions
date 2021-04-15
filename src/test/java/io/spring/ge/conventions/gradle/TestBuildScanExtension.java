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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.gradle.scan.plugin.BuildResult;
import com.gradle.scan.plugin.BuildScanDataObfuscation;
import com.gradle.scan.plugin.BuildScanExtension;
import com.gradle.scan.plugin.PublishedBuildScan;
import org.gradle.api.Action;

/**
 * A {@link BuildScanExtension} used for unit testing.
 *
 * @author Andy Wilkinson
 */
public final class TestBuildScanExtension implements BuildScanExtension {

	final TestBuildScanDataObfuscation obfuscation = new TestBuildScanDataObfuscation();

	final List<String> tags = new ArrayList<>();

	final Map<String, String> values = new HashMap<>();

	final Map<String, String> links = new HashMap<>();

	boolean captureTaskInputFiles;

	boolean publishAlways;

	boolean publishIfAuthenticated;

	String server;

	boolean uploadInBackground = true;

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
		action.execute(this.obfuscation);
	}

	@Override
	public void publishAlways() {
		this.publishAlways = true;
	}

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

}
