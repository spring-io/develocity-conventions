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

package io.spring.ge.conventions.gradle;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import io.spring.ge.conventions.core.ConfigurableBuildScan;
import io.spring.ge.conventions.core.ProcessRunner;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link AnonymousPublicationBuildScanConventions}.
 *
 * @author Andy Wilkinson
 */
class AnonymousPublicationBuildScanConventionsTests {

	private final TestProcessRunner processRunner = new TestProcessRunner();

	private final TestConfigurableBuildScan buildScan = new TestConfigurableBuildScan();

	@Test
	void buildScansAreConfiguredToUseDefaultPublicationBehaviour() {
		new AnonymousPublicationBuildScanConventions(this.processRunner).execute(this.buildScan);
		assertThat(this.buildScan.publishAlways).isFalse();
		assertThat(this.buildScan.publishIfAuthenticated).isFalse();
	}

	@Test
	void buildScansAreConfiguredToPublishToDefaultServer() {
		new AnonymousPublicationBuildScanConventions(this.processRunner).execute(this.buildScan);
		assertThat(this.buildScan.server).isNull();
	}

	public static final class TestConfigurableBuildScan implements ConfigurableBuildScan {

		private boolean publishAlways;

		private boolean publishIfAuthenticated;

		private String server;

		@Override
		public void background(Consumer<ConfigurableBuildScan> action) {
		}

		@Override
		public String server() {
			return this.server;
		}

		@Override
		public void link(String name, String url) {
		}

		@Override
		public void publishAlways() {
			this.publishAlways = true;
		}

		@Override
		public void publishIfAuthenticated() {
			this.publishIfAuthenticated = true;
		}

		@Override
		public void captureInputFiles(boolean capture) {
		}

		@Override
		public void server(String server) {
			this.server = server;
		}

		@Override
		public void tag(String tag) {
		}

		@Override
		public void value(String name, String value) {
		}

		@Override
		public void uploadInBackground(boolean uploadInBackground) {
		}

		@Override
		public void obfuscation(Consumer<ObfuscationConfigurer> configurer) {
		}

	}

	private static final class TestProcessRunner implements ProcessRunner {

		private final Map<List<String>, String> commandLineOutput = new HashMap<>();

		@Override
		public void run(Consumer<ProcessSpec> configurer) {
			ProcessSpec processSpec = mock(ProcessSpec.class);
			configurer.accept(processSpec);
			ArgumentCaptor<Object> commandLineCaptor = ArgumentCaptor.forClass(Object.class);
			verify(processSpec).commandLine(commandLineCaptor.capture());
			ArgumentCaptor<OutputStream> standardOut = ArgumentCaptor.forClass(OutputStream.class);
			verify(processSpec).standardOutput(standardOut.capture());
			List<Object> commandLine = commandLineCaptor.getAllValues();
			String output = this.commandLineOutput.get(commandLine);
			if (output != null) {
				try {
					standardOut.getValue().write(output.getBytes());
				}
				catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		}

	}

}
