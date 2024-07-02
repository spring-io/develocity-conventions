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

package io.spring.develocity.conventions.build;

import java.util.Arrays;

import org.gradle.api.Project;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.tasks.GenerateModuleMetadata;

public class MavenPublishConventions {

	void apply(Project project) {
		project.setGroup("io.spring.develocity.conventions");
		configureDeploymentRepository(project);
		configurePom(project);
		disableModuleMetadataGeneration(project);
	}

	private void configureDeploymentRepository(Project project) {
		if (project.hasProperty("distributionRepository")) {
			PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
			publishing.getRepositories().maven((maven) -> {
				maven.setUrl(project.property("distributionRepository"));
				maven.setName("deployment");
			});
		}
	}

	private void configurePom(Project project) {
		PublicationContainer publications = project.getExtensions()
			.getByType(PublishingExtension.class)
			.getPublications();
		publications.withType(MavenPublication.class).configureEach((mavenPublication) -> {
			mavenPublication.pom((pom) -> {
				pom.getName().set(project.getDescription());
				pom.getDescription().set(project.getDescription());
				pom.getUrl().set("https://github.com/spring-io/develocity-conventions");
				pom.organization((organization) -> {
					organization.getName().set("Pivotal Software, Inc.");
					organization.getUrl().set("https://spring.io");
				});
				pom.licenses((licenses) -> licenses.license((license) -> {
					license.getName().set("The Apache Software License, Version 2.0");
					license.getUrl().set("https://www.apache.org/licenses/LICENSE-2.0.txt");
				}));
				pom.scm((scm) -> {
					scm.getUrl().set("https://github.com/spring-io/develocity-conventions");
					scm.getConnection().set("scm:git:https://github.com/spring-io/develocity-conventions");
				});
				pom.developers((developers) -> {
					developers.developer((developer) -> {
						developer.getId().set("wilkinsona");
						developer.getName().set("Andy Wilkinson");
						developer.getEmail().set("awilkinson@pivotal.io");
						developer.getRoles().set(Arrays.asList("Project Lead"));
					});
				});
			});
		});
	}

	private void disableModuleMetadataGeneration(Project project) {
		project.getTasks()
			.withType(GenerateModuleMetadata.class,
					(generateModuleMetadata) -> generateModuleMetadata.setEnabled(false));
	}

}
