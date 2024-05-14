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

package io.spring.ge.conventions.gradle;

import java.io.OutputStream;
import java.util.function.Consumer;

import org.gradle.api.internal.ProcessOperations;
import org.gradle.process.ExecResult;
import org.gradle.process.ExecSpec;

/**
 * A {@link ProcessRunner} that delegates to Gradle's internal {@link ProcessOperations}.
 *
 * @author Andy Wilkinson
 */
class ProcessOperationsProcessRunner implements ProcessRunner {

	private final ProcessOperations processOperations;

	ProcessOperationsProcessRunner(ProcessOperations processOperations) {
		this.processOperations = processOperations;
	}

	@Override
	public void run(Consumer<ProcessSpec> configurer) {
		try {
			ExecResult exec = this.processOperations.exec((spec) -> configurer.accept(new ExecSpecProcessSpec(spec)));
		}
		catch (Exception ex) {
			throw new RunFailedException(ex);
		}
	}

	private final class ExecSpecProcessSpec implements ProcessSpec {

		private final ExecSpec execSpec;

		private ExecSpecProcessSpec(ExecSpec execSpec) {
			this.execSpec = execSpec;
		}

		@Override
		public void commandLine(Object... commandLine) {
			this.execSpec.commandLine(commandLine);
		}

		@Override
		public void standardOutput(OutputStream standardOutput) {
			this.execSpec.setStandardOutput(standardOutput);
		}

	}

}
