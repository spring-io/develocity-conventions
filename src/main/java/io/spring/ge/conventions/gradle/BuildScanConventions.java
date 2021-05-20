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

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.gradle.scan.plugin.BuildScanExtension;
import org.gradle.api.Action;

/**
 * {@link Action} that configures the {@link BuildScanExtension build scan} with Spring
 * conventions.
 *
 * @author Andy Wilkinson
 */
class BuildScanConventions implements Action<BuildScanExtension> {

	private static final String BAMBOO_RESULTS_ENV_VAR = "bamboo_resultsUrl";

	private static final String CIRCLECI_BUILD_URL_ENV_VAR = "CIRCLE_BUILD_URL";

	private final Map<String, String> env;

	private final ProcessRunner processRunner;

	BuildScanConventions(ProcessRunner processRunner) {
		this(processRunner, System.getenv());
	}

	BuildScanConventions(ProcessRunner processRunner, Map<String, String> env) {
		this.processRunner = processRunner;
		this.env = env;
	}

	/**
	 * Applies the conventions to the given {@code buildScan}.
	 * @param buildScan build scan to be configured
	 */
	@Override
	public void execute(BuildScanExtension buildScan) {
		buildScan.setCaptureTaskInputFiles(true);
		buildScan.obfuscation((obfuscation) -> obfuscation.ipAddresses(
				(addresses) -> addresses.stream().map((address) -> "0.0.0.0").collect(Collectors.toList())));
		configurePublishing(buildScan);
		tagBuildScan(buildScan);
		buildScan.background(this::addGitMetadata);
		addCiMetadata(buildScan);
		try {
			buildScan.setUploadInBackground(!isCi());
		}
		catch (NoSuchMethodError ex) {
			// GE Plugin version < 3.3. Continue
		}
	}

	/**
	 * Configures publishing of the build scan. The default implementation always
	 * publishes scans when authenticated and publishes them to
	 * {@code https://ge.spring.io}.
	 * @param buildScan build scan to configure
	 */
	protected void configurePublishing(BuildScanExtension buildScan) {
		buildScan.publishAlways();
		try {
			buildScan.getClass().getMethod("publishIfAuthenticated").invoke(buildScan);
		}
		catch (Exception ex) {
			throw new RuntimeException("Failed to invoke publishIfAuthenticated()", ex);
		}
		buildScan.setServer("https://ge.spring.io");
	}

	private void tagBuildScan(BuildScanExtension buildScan) {
		tagCiOrLocal(buildScan);
		tagJdk(buildScan);
		tagOperatingSystem(buildScan);
	}

	private void tagCiOrLocal(BuildScanExtension buildScan) {
		buildScan.tag(isCi() ? "CI" : "Local");
	}

	private boolean isCi() {
		if (isBamboo() || isCircleCi() || isConcourse() || isJenkins()) {
			return true;
		}
		return false;
	}

	private boolean isBamboo() {
		return this.env.containsKey(BAMBOO_RESULTS_ENV_VAR);
	}

	private boolean isCircleCi() {
		return this.env.containsKey(CIRCLECI_BUILD_URL_ENV_VAR);
	}

	private boolean isConcourse() {
		return this.env.containsKey("CI");
	}

	private boolean isJenkins() {
		return this.env.containsKey("JENKINS_URL");
	}

	private void tagJdk(BuildScanExtension buildScan) {
		buildScan.tag("JDK-" + getJdkVersion());
	}

	protected String getJdkVersion() {
		return System.getProperty("java.specification.version");
	}

	private void tagOperatingSystem(BuildScanExtension buildScan) {
		buildScan.tag(System.getProperty("os.name"));
	}

	private void addGitMetadata(BuildScanExtension buildScan) {
		run("git", "rev-parse", "--short=8", "--verify", "HEAD").standardOut((gitCommitId) -> {
			String commitIdLabel = "Git commit";
			buildScan.value(commitIdLabel, gitCommitId);
			String server = buildScan.getServer();
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

	private void addCiMetadata(BuildScanExtension buildScan) {
		if (isBamboo()) {
			buildScan.link("CI build", this.env.get(BAMBOO_RESULTS_ENV_VAR));
		}
		else if (isJenkins()) {
			String buildUrl = this.env.get("BUILD_URL");
			if (hasText(buildUrl)) {
				buildScan.link("CI build", buildUrl);
			}
		}
		else if (isCircleCi()) {
			buildScan.link("CI build", this.env.get(CIRCLECI_BUILD_URL_ENV_VAR));
		}
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
		this.processRunner.run((spec) -> {
			spec.commandLine(commandLine);
			spec.standardOutput(standardOutput);
		});
		return new RunResult(standardOutput.toString().trim());
	}

	private boolean hasText(String string) {
		return string != null && string.length() > 0;
	}

	private static final class RunResult {

		private final String standardOutput;

		private RunResult(String standardOutput) {
			this.standardOutput = standardOutput;
		}

		private void standardOut(Consumer<String> consumer) {
			if (this.standardOutput.length() > 0) {
				consumer.accept(this.standardOutput);
			}
		}

	}

}
