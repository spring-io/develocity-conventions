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

import java.util.Map;
import java.util.function.Function;

/**
 * Known continuous integration environments.
 *
 * @author Andy Wilkinson
 */
enum ContinuousIntegration {

	BAMBOO("Bamboo", "bamboo_resultsUrl"),

	CIRCLE_CI("CircleCI", "CIRCLE_BUILD_URL"),

	GITHUB_ACTIONS("GitHub Actions", "GITHUB_ACTIONS", (env) -> {
		String server = env.get("GITHUB_SERVER_URL");
		String repository = env.get("GITHUB_REPOSITORY");
		String runId = env.get("GITHUB_RUN_ID");
		return server + "/" + repository + "/actions/runs/" + runId;
	}),

	JENKINS("Jenkins", "JENKINS_URL", (env) -> env.get("BUILD_URL")),

	CONCOURSE("Concourse", "CI", (env) -> null);

	private final String name;

	private final String environmentVariable;

	private final Function<Map<String, String>, String> buildUrl;

	ContinuousIntegration(String name, String environmentVariable) {
		this(name, environmentVariable, (env) -> env.get(environmentVariable));
	}

	ContinuousIntegration(String name, String environmentVariable, Function<Map<String, String>, String> buildUrl) {
		this.name = name;
		this.environmentVariable = environmentVariable;
		this.buildUrl = buildUrl;
	}

	String buildUrlFrom(Map<String, String> env) {
		return this.buildUrl.apply(env);
	}

	@Override
	public String toString() {
		return this.name;
	}

	static ContinuousIntegration detect(Map<String, String> env) {
		for (ContinuousIntegration ci : values()) {
			if (env.containsKey(ci.environmentVariable)) {
				return ci;
			}
		}
		return null;
	}

}
