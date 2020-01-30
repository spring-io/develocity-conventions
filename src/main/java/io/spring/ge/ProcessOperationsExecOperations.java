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

import org.gradle.api.Action;
import org.gradle.api.internal.ProcessOperations;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecResult;
import org.gradle.process.ExecSpec;
import org.gradle.process.JavaExecSpec;

/**
 * An adapter for {@link ProcessOperations} to {@link ExecOperations}.
 *
 * @author Andy Wilkinson
 */
class ProcessOperationsExecOperations implements ExecOperations {

	private final ProcessOperations processOperations;

	ProcessOperationsExecOperations(ProcessOperations processOperations) {
		this.processOperations = processOperations;
	}

	@Override
	public ExecResult exec(Action<? super ExecSpec> spec) {
		return this.processOperations.exec(spec);
	}

	@Override
	public ExecResult javaexec(Action<? super JavaExecSpec> spec) {
		return this.processOperations.javaexec(spec);
	}

}
