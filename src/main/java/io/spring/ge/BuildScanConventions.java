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

package io.spring.ge;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.gradle.scan.plugin.BuildScanExtension;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.process.ExecOperations;

/**
 * {@link Action} that configures the {@link BuildScanExtension build scan} with Spring
 * conventions.
 *
 * @author Andy Wilkinson
 */
class BuildScanConventions implements Action<BuildScanExtension> {

	private static final String BAMBOO_RESULTS_ENV_VAR = "bamboo_resultsUrl";

	private final Map<String, String> env;

	private final ExecOperations execOperations;

	BuildScanConventions(ExecOperations execOperations) {
		this(execOperations, System.getenv());
	}

	BuildScanConventions(ExecOperations execOperations, Map<String, String> env) {
		this.execOperations = execOperations;
		this.env = env;
	}

	@Override
	public void execute(BuildScanExtension buildScan) {
		buildScan.setCaptureTaskInputFiles(true);
		buildScan.obfuscation((obfuscation) -> obfuscation.ipAddresses(
				(addresses) -> addresses.stream().map((address) -> "0.0.0.0").collect(Collectors.toList())));
		buildScan.publishAlways();
		try {
			buildScan.getClass().getMethod("publishIfAuthenticated").invoke(buildScan);
		}
		catch (Exception ex) {
			throw new GradleException("Failed to enable publishIfAuthenticated", ex);
		}
		buildScan.setServer("https://ge.spring.io");
		tagBuildScan(buildScan);
		buildScan.background(this::addGitMetadata);
		addCiMetadata(buildScan);
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
		if (isBamboo() || isConcourse()) {
			return true;
		}
		return false;
	}

	private boolean isBamboo() {
		return this.env.containsKey(BAMBOO_RESULTS_ENV_VAR);
	}

	private boolean isConcourse() {
		return this.env.containsKey("CI");
	}

	private void tagJdk(BuildScanExtension buildScan) {
		buildScan.tag("JDK-" + System.getProperty("java.specification.version"));
	}

	private void tagOperatingSystem(BuildScanExtension buildScan) {
		buildScan.tag(System.getProperty("os.name"));
	}

	private void addGitMetadata(BuildScanExtension buildScan) {
		exec("git", "rev-parse", "--short=8", "--verify", "HEAD").standardOut((gitCommitId) -> {
			String commitIdLabel = "Git commit";
			buildScan.value(commitIdLabel, gitCommitId);
			buildScan.link("Git commit build scans",
					buildScan.getServer() + createSearchUrl(commitIdLabel, gitCommitId));
		});
		getBranch().standardOut((gitBranchName) -> {
			buildScan.tag(gitBranchName);
			buildScan.value("Git branch", gitBranchName);
		});
		exec("git", "status", "--porcelain").standardOut((gitStatus) -> {
			buildScan.tag("dirty");
			buildScan.value("Git status", gitStatus);
		});
	}

	private void addCiMetadata(BuildScanExtension buildScan) {
		if (isBamboo()) {
			buildScan.link("CI build", this.env.get(BAMBOO_RESULTS_ENV_VAR));
		}
	}

	private ExecResult getBranch() {
		String branch = this.env.get("BRANCH");
		if (branch != null) {
			return new ExecResult(branch);
		}
		return exec("git", "rev-parse", "--abbrev-ref", "HEAD");
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

	private ExecResult exec(Object... commandLine) {
		ByteArrayOutputStream standardOutput = new ByteArrayOutputStream();
		this.execOperations.exec((spec) -> {
			spec.setCommandLine(commandLine);
			spec.setStandardOutput(standardOutput);
			spec.setWorkingDir(new File(".").getAbsolutePath());
		});
		return new ExecResult(standardOutput.toString().trim());
	}

	private static final class ExecResult {

		private final String standardOutput;

		private ExecResult(String standardOutput) {
			this.standardOutput = standardOutput;
		}

		private void standardOut(Consumer<String> consumer) {
			if (this.standardOutput.length() > 0) {
				consumer.accept(this.standardOutput);
			}
		}

	}

}
