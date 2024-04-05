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

package io.spring.ge.conventions.gradle;

import java.util.Map;

import com.gradle.develocity.agent.gradle.DevelocityConfiguration;
import com.gradle.develocity.agent.gradle.scan.BuildScanConfiguration;

/**
 * Conventions for build scans that are published anonymously to
 * {@code https://scans.gradle.com}.
 *
 * @author Andy Wilkinson
 */
public class AnonymousPublicationBuildScanConventions extends BuildScanConventions {

	public AnonymousPublicationBuildScanConventions(DevelocityConfiguration develocity, ProcessRunner processRunner,
			Map<String, String> env) {
		super(develocity, processRunner, env);
	}

	public AnonymousPublicationBuildScanConventions(DevelocityConfiguration develocity, ProcessRunner processRunner) {
		super(develocity, processRunner);
	}

	@Override
	protected void configurePublishing(BuildScanConfiguration buildScan) {
		// Use Gradle's defaults
	}

}
