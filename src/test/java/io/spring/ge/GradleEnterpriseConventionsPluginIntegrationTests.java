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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import com.gradle.enterprise.gradleplugin.GradleEnterprisePlugin;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integrations tests for {@link GradleEnterpriseConventionsPlugin}.
 *
 * @author Andy Wilkinson
 */
class GradleEnterpriseConventionsPluginIntegrationTests {

	@Test
	void givenGradle6WhenThePluginIsAppliedThenBuildScanConventionsAreApplied(@TempDir File projectDir) {
		prepareGradle6Project(projectDir);
		BuildResult result = build(projectDir, "6.0.1", "verifyBuildScanConfig");
		assertThat(result.getOutput()).contains("Build scan server: https://ge.spring.io");
	}

	@Test
	void givenGradle6WhenThePluginIsAppliedAndBuildScansAreDisabledThenBuildScanConventionsAreNotApplied(
			@TempDir File projectDir) {
		prepareGradle6Project(projectDir);
		BuildResult result = build(projectDir, "6.0.1", "verifyBuildScanConfig", "--no-scan");
		assertThat(result.getOutput()).contains("Build scan server: null");
	}

	@Test
	void givenGradle5WhenThePluginIsAppliedThenBuildScanConventionsAreApplied(@TempDir File projectDir) {
		prepareGradle5Project(projectDir);
		BuildResult result = build(projectDir, "5.6.4", "verifyBuildScanConfig");
		assertThat(result.getOutput()).contains("Build scan server: https://ge.spring.io");
	}

	@Test
	void givenGradle5WhenThePluginIsAppliedAndBuildScansAreDisabledThenBuildScanConventionsAreNotApplied(
			@TempDir File projectDir) {
		prepareGradle5Project(projectDir);
		BuildResult result = build(projectDir, "5.6.4", "verifyBuildScanConfig", "--no-scan");
		assertThat(result.getOutput()).contains("Build scan server: null");
	}

	private void prepareGradle5Project(File projectDir) {
		write(new File(projectDir, "build.gradle"), (writer) -> {
			writer.println("plugins {");
			writer.println("    id 'com.gradle.build-scan' version '3.1.1'");
			writer.println("    id 'io.spring.gradle-enterprise-conventions' version '" + version() + "'");
			writer.println("}");
			writer.println("task verifyBuildScanConfig {");
			writer.println("    doFirst {");
			writer.println("        println \"Build scan server: ${buildScan.server}\"");
			writer.println("    }");
			writer.println("}");
		});
	}

	private void prepareGradle6Project(File projectDir) {
		write(new File(projectDir, "settings.gradle"), (writer) -> {
			writer.println("plugins {");
			writer.println("    id 'com.gradle.enterprise' version '3.1.1'");
			writer.println("    id 'io.spring.gradle-enterprise-conventions' version '" + version() + "'");
			writer.println("}");
		});
		write(new File(projectDir, "build.gradle"), (writer) -> {
			writer.println("task verifyBuildScanConfig {");
			writer.println("    doFirst {");
			writer.println("        println \"Build scan server: ${buildScan.server}\"");
			writer.println("    }");
			writer.println("}");
		});
	}

	private void write(File file, Consumer<PrintWriter> consumer) {
		try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
			consumer.accept(writer);
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private String version() {
		Properties properties = new Properties();
		try (Reader reader = new FileReader("gradle.properties")) {
			properties.load(new FileReader("gradle.properties"));
			return properties.getProperty("version");
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private BuildResult build(File projectDir, String gradleVersion, String... arguments) {
		List<File> classpath = Arrays.asList(new File("bin/main"), new File("build/classes/java/main"),
				new File("build/resources/main"),
				new File(GradleEnterprisePlugin.class.getProtectionDomain().getCodeSource().getLocation().getFile()));
		return GradleRunner.create().withGradleVersion(gradleVersion).withProjectDir(projectDir)
				.withPluginClasspath(classpath).withArguments(arguments).build();
	}

}
