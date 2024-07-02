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

import io.spring.javaformat.gradle.SpringJavaFormatPlugin;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.api.plugins.quality.CheckstylePlugin;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;

public class JavaConventions {

	void apply(Project project) {
		configureJavaCompilation(project);
		configureSourceAndJavadocJars(project);
		configureSpringJavaFormat(project);
		configureCheckstyle(project);
		configureTestTasks(project);
	}

	private void configureJavaCompilation(Project project) {
		JavaPluginExtension javaPluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);
		javaPluginExtension.setSourceCompatibility(JavaVersion.VERSION_1_8);
		javaPluginExtension.setTargetCompatibility(JavaVersion.VERSION_1_8);
		TaskContainer tasks = project.getTasks();
		tasks.withType(JavaCompile.class,
				(javaCompile) -> javaCompile.getOptions()
					.getCompilerArgs()
					.addAll(Arrays.asList("-Werror", "-Xlint:unchecked", "-Xlint:deprecation", "-Xlint:rawtypes",
							"-Xlint:varargs")));
	}

	private void configureSourceAndJavadocJars(Project project) {
		JavaPluginExtension javaPluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);
		javaPluginExtension.withJavadocJar();
		javaPluginExtension.withSourcesJar();
	}

	private void configureSpringJavaFormat(Project project) {
		project.getPlugins().apply(SpringJavaFormatPlugin.class);
	}

	private void configureCheckstyle(Project project) {
		String javaFormatVersion = SpringJavaFormatPlugin.class.getPackage().getImplementationVersion();
		project.getPlugins().apply(CheckstylePlugin.class);
		project.getDependencies()
			.add("checkstyle", "io.spring.javaformat:spring-javaformat-checkstyle:" + javaFormatVersion);
		CheckstyleExtension checkstyleExtension = project.getExtensions().getByType(CheckstyleExtension.class);
		checkstyleExtension.setToolVersion("9.3");
		checkstyleExtension.setConfig(project.getResources()
			.getText()
			.fromArchiveEntry(
					project.getConfigurations()
						.getByName("checkstyle")
						.filter((file) -> file.getName().startsWith("spring-javaformat-checkstyle")),
					"io/spring/javaformat/checkstyle/checkstyle.xml"));
	}

	private void configureTestTasks(Project project) {
		project.getTasks().withType(Test.class, (test) -> test.useJUnitPlatform());
	}

}
