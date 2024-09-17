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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import com.gradle.develocity.agent.gradle.DevelocityPlugin;
import io.spring.develocity.conventions.core.ConfigurableBuildScan;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integrations tests for {@link DevelocityConventionsPlugin}.
 *
 * @author Andy Wilkinson
 */
class DevelocityConventionsPluginIntegrationTests {

	@Test
	void whenThePluginIsAppliedThenBuildScanConventionsAreApplied(@TempDir File projectDir) {
		prepareProject(projectDir);
		BuildResult result = build(projectDir, "verifyBuildScanConfig");
		assertThat(result.getOutput()).contains("Build scan server: https://ge.spring.io");
		assertThat(result.getOutput()).contains("Capture task input files: true");
	}

	@Test
	void whenThePluginIsAppliedThenBuildCacheConventionsAreApplied(@TempDir File projectDir) {
		prepareProject(projectDir);
		BuildResult result = build(projectDir, "verifyBuildCacheConfig");
		assertThat(result.getOutput()).contains("Build cache server: https://ge.spring.io");
	}

	@Test
	void whenThePluginIsAppliedAndBuildScansAreDisabledThenBuildScanConventionsAreNotApplied(@TempDir File projectDir) {
		prepareProject(projectDir);
		BuildResult result = build(projectDir, "verifyBuildScanConfig", "--no-scan");
		assertThat(result.getOutput()).contains("Build scan server: null");
		assertThat(result.getOutput()).contains("Capture task input files: false");
	}

	@Test
	void whenThePluginIsAppliedAndTheSpringBuildTypeIsNotOssThenBuildScanConventionsAreNotApplied(
			@TempDir File projectDir) {
		prepareProject(projectDir);
		write(new File(projectDir, "gradle.properties"), (writer) -> writer.println("spring.build-type=other"));
		BuildResult result = build(projectDir, "verifyBuildScanConfig");
		assertThat(result.getOutput()).contains("Build scan server: null");
		assertThat(result.getOutput()).contains("Capture task input files: false");
	}

	@Test
	void whenThePluginIsAppliedAndTheSpringBuildTypeIsOssThenBuildScanConventionsAreApplied(@TempDir File projectDir) {
		prepareProject(projectDir);
		write(new File(projectDir, "gradle.properties"), (writer) -> writer.println("spring.build-type=oss"));
		BuildResult result = build(projectDir, "verifyBuildScanConfig");
		assertThat(result.getOutput()).contains("Build scan server: https://ge.spring.io");
		assertThat(result.getOutput()).contains("Capture task input files: true");
	}

	@Test
	void whenThePluginIsAppliedAndPropertiesTaskIsExecutedThenBuildScanConventionsAreNotApplied(
			@TempDir File projectDir) {
		prepareProject(projectDir);
		BuildResult result = build(projectDir, "properties", "verifyBuildScanConfig", "--no-scan");
		assertThat(result.getOutput()).contains("Build scan server: null");
		assertThat(result.getOutput()).contains("Capture task input files: false");
	}

	@Test
	void givenMultiProjectBuildWhenThePluginIsAppliedAndPropertiesTaskIsExecutedThenBuildScanConventionsAreNotApplied(
			@TempDir File projectDir) {
		prepareMultiModuleProject(projectDir);
		BuildResult result = build(projectDir, "sub:properties", "sub:verifyBuildScanConfig", "--no-scan");
		assertThat(result.getOutput()).contains("Build scan server: null");
		assertThat(result.getOutput()).contains("Capture task input files: false");
	}

	@Test
	void whenThePluginIsAppliedAndScanIsSpecifiedThenServerIsNotCustomized(@TempDir File projectDir) {
		prepareProject(projectDir);
		BuildResult result = build(projectDir, "verifyBuildScanConfig", "--scan");
		assertThat(result.getOutput()).contains("Build scan server: null");
		assertThat(result.getOutput()).contains("Capture task input files: true");
	}

	@Test
	void whenThePluginIsAppliedAndBuildCacheIsDisabledThenBuildCacheConventionsAreNotApplied(@TempDir File projectDir) {
		prepareProject(projectDir);
		BuildResult result = build(projectDir, "verifyBuildCacheConfig", "--no-build-cache");
		assertThat(result.getOutput()).contains("Build cache server: null");
	}

	@Test
	void whenThePluginIsAppliedAndTheSpringBuildTypeIsNotOssThenBuildCacheConventionsAreNotApplied(
			@TempDir File projectDir) {
		prepareProject(projectDir);
		write(new File(projectDir, "gradle.properties"), (writer) -> writer.println("spring.build-type=other"));
		BuildResult result = build(projectDir, "verifyBuildCacheConfig");
		assertThat(result.getOutput()).contains("Build cache server: null");
	}

	@Test
	void whenThePluginIsAppliedAndTheSpringBuildTypeIsOssThenBuildCacheConventionsAreApplied(@TempDir File projectDir) {
		prepareProject(projectDir);
		write(new File(projectDir, "gradle.properties"), (writer) -> writer.println("spring.build-type=oss"));
		BuildResult result = build(projectDir, "verifyBuildCacheConfig");
		assertThat(result.getOutput()).contains("Build cache server: https://ge.spring.io");
	}

	private void prepareProject(File projectDir) {
		write(new File(projectDir, "gradle.properties"), (writer) -> writer.println("org.gradle.caching=true"));
		write(new File(projectDir, "settings.gradle"), (writer) -> {
			writer.println("plugins {");
			writer.println("    id 'io.spring.develocity.conventions' version '" + version() + "'");
			writer.println("}");
			writer.println("gradle.afterProject { project -> project.ext['settings'] = settings }");
		});
		write(new File(projectDir, "build.gradle"), (writer) -> {
			writer.println("task verifyBuildScanConfig {");
			writer.println("    doFirst {");
			writer.println("        println \"Build scan server: ${buildScan.server}\"");
			writer.println("        println \"Capture task input files: ${buildScan.captureTaskInputFiles}\"");
			writer.println("    }");
			writer.println("}");
			writer.println("task verifyBuildCacheConfig {");
			writer.println("    doFirst {");
			writer.println(
					"        println \"Build cache server: ${project.ext['settings'].buildCache?.remote?.server}\"");
			writer.println("    }");
			writer.println("}");
		});
	}

	private void prepareMultiModuleProject(File projectDir) {
		write(new File(projectDir, "gradle.properties"), (writer) -> writer.println("org.gradle.caching=true"));
		write(new File(projectDir, "settings.gradle"), (writer) -> {
			writer.println("plugins {");
			writer.println("    id 'io.spring.develocity.conventions' version '" + version() + "'");
			writer.println("}");
			writer.println("include 'sub'");
			writer.println("gradle.afterProject { project -> project.ext['settings'] = settings }");
		});
		write(new File(new File(projectDir, "sub"), "build.gradle"), (writer) -> {
			writer.println("task verifyBuildScanConfig {");
			writer.println("    doFirst {");
			writer.println("        println \"Build scan server: ${buildScan.server}\"");
			writer.println("        println \"Capture task input files: ${buildScan.captureTaskInputFiles}\"");
			writer.println("    }");
			writer.println("}");
			writer.println("task verifyBuildCacheConfig {");
			writer.println("    doFirst {");
			writer.println(
					"        println \"Build cache server: ${project.ext['settings'].buildCache?.remote?.server}\"");
			writer.println("    }");
			writer.println("}");
		});
	}

	private void write(File file, Consumer<PrintWriter> consumer) {
		file.getParentFile().mkdirs();
		try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
			consumer.accept(writer);
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private String version() {
		Properties properties = new Properties();
		try (Reader reader = new FileReader("../gradle.properties")) {
			properties.load(reader);
			return properties.getProperty("version");
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private BuildResult build(File projectDir, String... arguments) {
		List<File> classpath = Arrays.asList(new File("bin/main"), new File("build/classes/java/main"),
				new File("build/resources/main"),
				new File(DevelocityPlugin.class.getProtectionDomain().getCodeSource().getLocation().getFile()),
				new File(ConfigurableBuildScan.class.getProtectionDomain().getCodeSource().getLocation().getFile()));
		List<String> augmentedArguments = new ArrayList<>(Arrays.asList(arguments));
		augmentedArguments.add("--stacktrace");
		return GradleRunner.create()
			.withProjectDir(projectDir)
			.withPluginClasspath(classpath)
			.withArguments(augmentedArguments)
			.build();
	}

}
