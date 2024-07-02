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

package io.spring.ge.conventions.core;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.spring.ge.conventions.core.ProcessRunner.RunFailedException;

/**
 * Conventions that are applied to build scans for Maven and Gradle builds. Spring
 * conventions.
 *
 * @author Andy Wilkinson
 */
public class BuildScanConventions {

	private final ProcessRunner processRunner;

	private final Map<String, String> env;

	public BuildScanConventions(ProcessRunner processRunner) {
		this(processRunner, System.getenv());
	}

	protected BuildScanConventions(ProcessRunner processRunner, Map<String, String> env) {
		this.processRunner = processRunner;
		this.env = env;
	}

	/**
	 * Applies the conventions to the given {@code develocity} and {@code buildScan}.
	 * @param develocity develocity to be configured
	 * @param buildScan build scan to be configured
	 */
	public void execute(ConfigurableDevelocity develocity, ConfigurableBuildScan buildScan) {
		buildScan.obfuscation((obfuscation) -> obfuscation
			.ipAddresses((addresses) -> addresses.stream().map((address) -> "0.0.0.0").collect(Collectors.toList())));
		configurePublishing(develocity, buildScan);
		ContinuousIntegration ci = ContinuousIntegration.detect(this.env);
		tagBuildScan(buildScan, ci);
		buildScan.background((backgrounded) -> addGitMetadata(develocity, backgrounded));
		buildScan.background(this::addDockerMetadata);
		buildScan.background(this::addDockerComposeMetadata);
		addCiMetadata(buildScan, ci);
		buildScan.uploadInBackground(ci == null);
		buildScan.captureInputFiles(true);
	}

	/**
	 * Configures publishing of the build scan. The default implementation always
	 * publishes scans when authenticated and publishes them to
	 * {@code https://ge.spring.io}.
	 * @param develocity develocity to configure
	 * @param buildScan build scan to configure
	 *
	 */
	protected void configurePublishing(ConfigurableDevelocity develocity, ConfigurableBuildScan buildScan) {
		buildScan.publishIfAuthenticated();
		develocity.setServer("https://ge.spring.io");
	}

	private void tagBuildScan(ConfigurableBuildScan buildScan, ContinuousIntegration ci) {
		tagCiOrLocal(buildScan, ci);
		tagJdk(buildScan);
		tagOperatingSystem(buildScan);
	}

	private void tagCiOrLocal(ConfigurableBuildScan buildScan, ContinuousIntegration ci) {
		buildScan.tag((ci != null) ? "CI" : "Local");
	}

	private void tagJdk(ConfigurableBuildScan buildScan) {
		buildScan.tag("JDK-" + getJdkVersion());
	}

	protected String getJdkVersion() {
		return System.getProperty("java.specification.version");
	}

	private void tagOperatingSystem(ConfigurableBuildScan buildScan) {
		buildScan.tag(System.getProperty("os.name"));
	}

	private void addGitMetadata(ConfigurableDevelocity develocity, ConfigurableBuildScan buildScan) {
		run("git", "rev-parse", "--short=8", "--verify", "HEAD").standardOut((gitCommitId) -> {
			String commitIdLabel = "Git commit";
			buildScan.value(commitIdLabel, gitCommitId);

			String server = develocity.getServer();
			if (server != null) {
				buildScan.link("Git commit build scans", server + createSearchUrl(commitIdLabel, gitCommitId));
			}
		});
		getBranch().standardOut((gitBranchName) -> {
			buildScan.tag(gitBranchName);
			buildScan.value("Git branch", gitBranchName);
		});
		run("git", "status", "--porcelain").standardOut((gitStatus) -> {
			buildScan.tag("dirty");
			buildScan.value("Git status", gitStatus);
		});
	}

	private void addDockerMetadata(ConfigurableBuildScan buildScan) {
		run("docker", "--version").standardOut((dockerVersion) -> buildScan.value("Docker", dockerVersion));
	}

	private void addDockerComposeMetadata(ConfigurableBuildScan buildScan) {
		run("docker", "compose", "version")
			.standardOut((dockerComposeVersion) -> buildScan.value("Docker Compose", dockerComposeVersion));
	}

	private void addCiMetadata(ConfigurableBuildScan buildScan, ContinuousIntegration ci) {
		if (ci == null) {
			return;
		}
		String buildUrl = ci.buildUrlFrom(this.env);
		if (hasText(buildUrl)) {
			buildScan.link("CI build", buildUrl);
		}
		buildScan.value("CI provider", ci.toString());
	}

	private RunResult getBranch() {
		String branch = this.env.get("BRANCH");
		if (branch != null) {
			return new RunResult(branch);
		}
		return run("git", "rev-parse", "--abbrev-ref", "HEAD");
	}

	private String createSearchUrl(String name, String value) {
		return "/scans?search.names=" + encodeURL(name) + "&search.values=" + encodeURL(value);
	}

	private String encodeURL(String url) {
		try {
			return URLEncoder.encode(url, "UTF-8");
		}
		catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

	private RunResult run(Object... commandLine) {
		ByteArrayOutputStream standardOutput = new ByteArrayOutputStream();
		try {
			this.processRunner.run((spec) -> {
				spec.commandLine(commandLine);
				spec.standardOutput(standardOutput);
			});
			return new RunResult(standardOutput.toString().trim());
		}
		catch (RunFailedException ex) {
			return new RunResult();
		}
	}

	private boolean hasText(String string) {
		return string != null && string.length() > 0;
	}

	private static final class RunResult {

		private final String standardOutput;

		private RunResult() {
			this.standardOutput = null;
		}

		private RunResult(String standardOutput) {
			this.standardOutput = standardOutput;
		}

		private void standardOut(Consumer<String> consumer) {
			if (this.standardOutput != null && this.standardOutput.length() > 0) {
				consumer.accept(this.standardOutput);
			}
		}

	}

}
