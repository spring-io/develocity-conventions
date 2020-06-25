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

package io.spring.ge.core;

import java.io.OutputStream;
import java.util.function.Consumer;

/**
 * Minimal API for running a process.
 *
 * @author Andy Wilkinson
 */
public interface ProcessRunner {

	/**
	 * Runs the process described by the process spec.
	 * @param configurer configures the {@link ProcessSpec}
	 */
	void run(Consumer<ProcessSpec> configurer);

	/**
	 * A spec for running a process.
	 */
	interface ProcessSpec {

		/**
		 * The command line to be used to run the process.
		 * @param commandLine the command line
		 */
		void commandLine(Object... commandLine);

		/**
		 * Configures the stream to which the process's standard output should be written.
		 * @param standardOutput the stream for standard output
		 */
		void standardOutput(OutputStream standardOutput);

	}

}
