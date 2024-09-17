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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Properties;

import javax.inject.Inject;

import com.gradle.develocity.agent.gradle.DevelocityConfiguration;
import com.gradle.develocity.agent.gradle.DevelocityPlugin;
import com.gradle.develocity.agent.gradle.scan.BuildScanConfiguration;
import io.spring.develocity.conventions.core.BuildCacheConventions;
import io.spring.develocity.conventions.core.BuildScanConventions;
import org.gradle.StartParameter;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.gradle.api.internal.ProcessOperations;

/**
 * {@link Plugin plugin} for configuring the use of Develocity hosted at
 * <a href="https://ge.spring.io">ge.spring.io</a>.
 *
 * @author Andy Wilkinson
 */
public class DevelocityConventionsPlugin implements Plugin<Settings> {

	private final ProcessOperations processOperations;

	@Inject
	public DevelocityConventionsPlugin(ProcessOperations processOperations) {
		this.processOperations = processOperations;
	}

	@Override
	public void apply(Settings settings) {
		settings.getPlugins().apply(DevelocityPlugin.class);
		DevelocityConfiguration extension = settings.getExtensions().getByType(DevelocityConfiguration.class);
		if (!isOssBuild(settings)) {
			extension
				.buildScan((buildScan) -> buildScan.publishing((publishing) -> publishing.onlyIf((context) -> false)));
			return;
		}
		if (isBuildScanEnabled(settings)) {
			configureBuildScanConventions(extension, extension.getBuildScan(), settings.getStartParameter(),
					settings.getRootDir());
		}
		if (settings.getStartParameter().isBuildCacheEnabled()) {
			settings.buildCache((buildCacheConfiguration) -> new BuildCacheConventions()
				.execute(new GradleConfigurableBuildCache(extension.getBuildCache(), buildCacheConfiguration)));
		}
	}

	private boolean isOssBuild(Settings settings) {
		Properties properties = new Properties();
		File propertiesFile = new File(settings.getRootDir(), "gradle.properties");
		if (propertiesFile.exists()) {
			try (Reader reader = new FileReader(propertiesFile)) {
				properties.load(reader);
			}
			catch (IOException ex) {
				throw new UncheckedIOException(ex);
			}
		}
		String buildType = properties.getProperty("spring.build-type");
		return buildType == null || "oss".equals(buildType);
	}

	private boolean isBuildScanEnabled(Settings settings) {
		StartParameter startParameter = settings.getStartParameter();
		return !startParameter.isNoBuildScan() && !containsPropertiesTask(startParameter);
	}

	private boolean containsPropertiesTask(StartParameter startParameter) {
		for (String taskName : startParameter.getTaskNames()) {
			if (taskName.equals("properties") || taskName.endsWith(":properties")) {
				return true;
			}
		}
		return false;
	}

	private void configureBuildScanConventions(DevelocityConfiguration develocity, BuildScanConfiguration buildScan,
			StartParameter startParameter, File rootDir) {
		ProcessOperationsProcessRunner processRunner = new ProcessOperationsProcessRunner(
				new WorkingDirectoryProcessOperations(this.processOperations, rootDir));
		if (startParameter.isBuildScan()) {
			new AnonymousPublicationBuildScanConventions(processRunner) {

				@Override
				protected String getJdkVersion() {
					String toolchainVersion = startParameter.getProjectProperties().get("toolchainVersion");
					return (toolchainVersion != null) ? toolchainVersion : super.getJdkVersion();
				}

			}.execute(new GradleConfigurableDevelocity(develocity), new GradleConfigurableBuildScan(buildScan));
		}
		else {
			new BuildScanConventions(processRunner) {

				@Override
				protected String getJdkVersion() {
					String toolchainVersion = startParameter.getProjectProperties().get("toolchainVersion");
					return (toolchainVersion != null) ? toolchainVersion : super.getJdkVersion();
				}

			}.execute(new GradleConfigurableDevelocity(develocity), new GradleConfigurableBuildScan(buildScan));
		}
	}

}
