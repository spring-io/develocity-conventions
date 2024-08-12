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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.spring.develocity.conventions.core.ProcessRunner;

/**
 * {@link ProcessRunner} implementation that uses {@link ProcessBuilder}.
 *
 * @author Andy Wilkinson
 */
class ProcessBuilderProcessRunner implements ProcessRunner {

	@Override
	public void run(Consumer<ProcessSpec> configurer) {
		ProcessBuilder processBuilder = new ProcessBuilder();
		ProcessBuilderProcessSpec spec = new ProcessBuilderProcessSpec(processBuilder);
		configurer.accept(spec);
		try {
			processBuilder.start().waitFor();
			Files.copy(spec.output, spec.outputStream);
		}
		catch (Exception ex) {
			throw new RunFailedException(ex);
		}
	}

	private static final class ProcessBuilderProcessSpec implements ProcessSpec {

		private final ProcessBuilder processBuilder;

		private final Path output;

		private OutputStream outputStream;

		private ProcessBuilderProcessSpec(ProcessBuilder processBuilder) {
			this.processBuilder = processBuilder;
			try {
				this.output = Files.createTempFile("output", "txt");
			}
			catch (IOException ex) {
				throw new IllegalStateException(ex);
			}
		}

		@Override
		public void commandLine(Object... commandLine) {
			List<String> command = Stream.of(commandLine).map(Object::toString).collect(Collectors.toList());
			this.processBuilder.command(command);
		}

		@Override
		public void standardOutput(OutputStream standardOutput) {
			this.processBuilder.redirectOutput(this.output.toFile());
			this.outputStream = standardOutput;
		}

	}

}
