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

import java.io.File;

import javax.inject.Inject;

import com.gradle.develocity.agent.gradle.DevelocityConfiguration;
import com.gradle.develocity.agent.gradle.DevelocityPlugin;
import com.gradle.develocity.agent.gradle.scan.BuildScanConfiguration;
import org.gradle.StartParameter;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.gradle.api.internal.ProcessOperations;

/**
 * {@link Plugin plugin} for configuring the use of Gradle Enterprise hosted at
 * <a href="https://ge.spring.io">ge.spring.io</a>.
 *
 * @author Andy Wilkinson
 */
public class GradleEnterpriseConventionsPlugin implements Plugin<Settings> {

	private final ProcessOperations processOperations;

	@Inject
	public GradleEnterpriseConventionsPlugin(ProcessOperations processOperations) {
		this.processOperations = processOperations;
	}

	@Override
	public void apply(Settings settings) {
		settings.getPlugins().withType(DevelocityPlugin.class, (plugin) -> {
			DevelocityConfiguration extension = settings.getExtensions().getByType(DevelocityConfiguration.class);
			configureBuildScanConventions(extension, extension.getBuildScan(), settings.getStartParameter(),
					settings.getRootDir());
		});
		if (settings.getStartParameter().isBuildCacheEnabled()) {
			settings
				.buildCache((buildCacheConfiguration) -> new BuildCacheConventions().execute(buildCacheConfiguration));
		}
	}

	private void configureBuildScanConventions(DevelocityConfiguration develocity, BuildScanConfiguration buildScan,
			StartParameter startParameter, File rootDir) {
		if (startParameter.isNoBuildScan() || containsPropertiesTask(startParameter)) {
			return;
		}
		ProcessOperationsProcessRunner processRunner = new ProcessOperationsProcessRunner(
				new WorkingDirectoryProcessOperations(this.processOperations, rootDir));
		if (startParameter.isBuildScan()) {
			new AnonymousPublicationBuildScanConventions(develocity, processRunner) {

				@Override
				protected String getJdkVersion() {
					String toolchainVersion = startParameter.getProjectProperties().get("toolchainVersion");
					return (toolchainVersion != null) ? toolchainVersion : super.getJdkVersion();
				}

			}.execute(buildScan);
		}
		else {
			new BuildScanConventions(develocity, processRunner) {

				@Override
				protected String getJdkVersion() {
					String toolchainVersion = startParameter.getProjectProperties().get("toolchainVersion");
					return (toolchainVersion != null) ? toolchainVersion : super.getJdkVersion();
				}

			}.execute(buildScan);
		}
	}

	private boolean containsPropertiesTask(StartParameter startParameter) {
		for (String taskName : startParameter.getTaskNames()) {
			if (taskName.equals("properties") || taskName.endsWith(":properties")) {
				return true;
			}
		}
		return false;
	}

}
