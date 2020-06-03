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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.gradle.scan.plugin.BuildScanExtension;
import com.gradle.scan.plugin.internal.api.BuildScanExtensionWithHiddenFeatures;
import org.gradle.api.Action;
import org.gradle.api.internal.ProcessOperations;

/**
 * {@link Action} that configures the {@link BuildScanExtension build scan} with Spring
 * conventions.
 *
 * @author Andy Wilkinson
 */
class BuildScanConventions implements Action<BuildScanExtension> {

	private static final String BAMBOO_RESULTS_ENV_VAR = "bamboo_resultsUrl";

	private final Map<String, String> env;

	private final ProcessOperations processOperations;

	BuildScanConventions(ProcessOperations processOperations) {
		this(processOperations, System.getenv());
	}

	BuildScanConventions(ProcessOperations execOperations, Map<String, String> env) {
		this.processOperations = execOperations;
		this.env = env;
	}

	@Override
	public void execute(BuildScanExtension buildScan) {
		buildScan.setCaptureTaskInputFiles(true);
		buildScan.getObfuscation().ipAddresses(
				(addresses) -> addresses.stream().map((address) -> "0.0.0.0").collect(Collectors.toList()));
		buildScan.publishAlways();
		((BuildScanExtensionWithHiddenFeatures) buildScan).publishIfAuthenticated();
		buildScan.setServer("https://ge.spring.io");
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

	private void tagBuildScan(BuildScanExtension buildScan) {
		tagCiOrLocal(buildScan);
		tagJdk(buildScan);
		tagOperatingSystem(buildScan);
	}

	private void tagCiOrLocal(BuildScanExtension buildScan) {
		buildScan.tag(isCi() ? "CI" : "Local");
	}

	private boolean isCi() {
		if (isBamboo() || isConcourse() || isJenkins()) {
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

	private boolean isJenkins() {
		return this.env.containsKey("JENKINS_URL");
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
		else if (isJenkins()) {
			String buildUrl = this.env.get("BUILD_URL");
			if (hasText(buildUrl)) {
				buildScan.link("CI build", buildUrl);
			}
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
		this.processOperations.exec((spec) -> {
			spec.setCommandLine(commandLine);
			spec.setStandardOutput(standardOutput);
		});
		return new ExecResult(standardOutput.toString().trim());
	}

	private boolean hasText(String string) {
		return string != null && string.length() > 0;
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
