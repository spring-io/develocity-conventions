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

import javax.inject.Inject;

import com.gradle.enterprise.gradleplugin.GradleEnterpriseExtension;
import com.gradle.enterprise.gradleplugin.GradleEnterprisePlugin;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.gradle.api.internal.ProcessOperations;
import org.gradle.process.ExecOperations;

/**
 * {@link Settings} {@link Plugin plugin} for configuring the use of Gradle Enterprise
 * hosted at <a href="https://ge.spring.io">ge.spring.io</a>.
 *
 * @author Andy Wilkinson
 */
public class GradleEnterpriseConventionsPlugin implements Plugin<Settings> {

	private final ExecOperations execOperations;

	@Inject
	public GradleEnterpriseConventionsPlugin(ProcessOperations processOperations) {
		this.execOperations = new ProcessOperationsExecOperations(processOperations);
	}

	@Override
	public void apply(Settings settings) {
		settings.getPlugins().withType(GradleEnterprisePlugin.class, (plugin) -> {
			GradleEnterpriseExtension extension = settings.getExtensions().getByType(GradleEnterpriseExtension.class);
			extension.buildScan(new BuildScanConventions(this.execOperations));
		});
		settings.buildCache(new BuildCacheConventions());
	}

}
