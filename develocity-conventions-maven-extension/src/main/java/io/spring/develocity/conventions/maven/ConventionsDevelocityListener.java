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

package io.spring.develocity.conventions.maven;

import com.gradle.develocity.agent.maven.api.DevelocityApi;
import com.gradle.develocity.agent.maven.api.DevelocityListener;
import io.spring.develocity.conventions.core.BuildCacheConventions;
import io.spring.develocity.conventions.core.BuildScanConventions;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;

/**
 * {@link DevelocityListener} for configuring the use of Develocity hosted at
 * <a href="https://ge.spring.io">ge.spring.io</a>.
 *
 * @author Andy Wilkinson
 */
@Component(role = DevelocityListener.class, hint = "convention-develocity-maven-extension",
		description = "Develocity conventions Maven extension")
public class ConventionsDevelocityListener implements DevelocityListener {

	@Override
	public void configure(DevelocityApi develocity, MavenSession mavenSession) throws Exception {
		new BuildScanConventions(new ProcessBuilderProcessRunner()).execute(new MavenConfigurableDevelocity(develocity),
				new MavenConfigurableBuildScan(develocity.getBuildScan()));
		new BuildCacheConventions().execute(new MavenConfigurableBuildCache(develocity.getBuildCache()));
	}

}
