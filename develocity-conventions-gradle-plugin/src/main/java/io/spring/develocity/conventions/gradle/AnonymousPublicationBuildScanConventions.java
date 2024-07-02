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

import java.util.Map;

import io.spring.develocity.conventions.core.BuildScanConventions;
import io.spring.develocity.conventions.core.ConfigurableBuildScan;
import io.spring.develocity.conventions.core.ConfigurableDevelocity;
import io.spring.develocity.conventions.core.ProcessRunner;

/**
 * Conventions for build scans that are published anonymously to
 * {@code https://scans.gradle.com}.
 *
 * @author Andy Wilkinson
 */
class AnonymousPublicationBuildScanConventions extends BuildScanConventions {

	AnonymousPublicationBuildScanConventions(ProcessRunner processRunner) {
		super(processRunner);
	}

	AnonymousPublicationBuildScanConventions(ProcessRunner processRunner, Map<String, String> env) {
		super(processRunner, env);
	}

	@Override
	protected void configurePublishing(ConfigurableDevelocity develocity, ConfigurableBuildScan buildScan) {
		// Use Gradle's defaults
	}

}
