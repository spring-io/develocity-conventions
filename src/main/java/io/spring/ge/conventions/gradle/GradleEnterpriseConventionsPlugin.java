/*
 * Copyright 2020-2023 the original author or authors.
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

import com.gradle.enterprise.gradleplugin.GradleEnterpriseExtension;
import com.gradle.enterprise.gradleplugin.GradleEnterprisePlugin;
import com.gradle.scan.plugin.BuildScanExtension;
import org.gradle.StartParameter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.internal.ProcessOperations;

/**
 * {@link Plugin plugin} for configuring the use of Gradle Enterprise hosted at
 * <a href="https://ge.spring.io">ge.spring.io</a>.
 *
 * @author Andy Wilkinson
 */
public class GradleEnterpriseConventionsPlugin implements Plugin<Object> {

	private final ProcessOperations processOperations;

	@Inject
	public GradleEnterpriseConventionsPlugin(ProcessOperations processOperations) {
		this.processOperations = processOperations;
	}

	@Override
	public void apply(Object target) {
		if (target instanceof Settings) {
			apply((Settings) target);
		}
		else if (target instanceof Project) {
			apply((Project) target);
		}
	}

	private void apply(Settings settings) {
		settings.getPlugins().withType(GradleEnterprisePlugin.class, (plugin) -> {
			GradleEnterpriseExtension extension = settings.getExtensions().getByType(GradleEnterpriseExtension.class);
			configureBuildScanConventions(extension.getBuildScan(), settings.getStartParameter(),
					settings.getRootDir());
		});
		if (settings.getStartParameter().isBuildCacheEnabled()) {
			settings
				.buildCache((buildCacheConfiguration) -> new BuildCacheConventions().execute(buildCacheConfiguration));
		}
	}

	private void apply(Project project) {
		project.getPlugins()
			.withId("com.gradle.build-scan",
					(plugin) -> configureBuildScanConventions(
							project.getExtensions().getByType(BuildScanExtension.class),
							project.getGradle().getStartParameter(), project.getRootDir()));
	}

	private void configureBuildScanConventions(BuildScanExtension buildScan, StartParameter startParameter,
			File rootDir) {
		if (!startParameter.isNoBuildScan()) {
			ProcessOperationsProcessRunner processRunner = new ProcessOperationsProcessRunner(
					new WorkingDirectoryProcessOperations(this.processOperations, rootDir));
			if (startParameter.isBuildScan()) {
				new AnonymousPublicationBuildScanConventions(processRunner) {

					@Override
					protected String getJdkVersion() {
						String toolchainVersion = startParameter.getProjectProperties().get("toolchainVersion");
						return (toolchainVersion != null) ? toolchainVersion : super.getJdkVersion();
					}

				}.execute(buildScan);
			}
			else {
				new BuildScanConventions(processRunner) {

					@Override
					protected String getJdkVersion() {
						String toolchainVersion = startParameter.getProjectProperties().get("toolchainVersion");
						return (toolchainVersion != null) ? toolchainVersion : super.getJdkVersion();
					}

				}.execute(buildScan);
			}
		}
	}

}
