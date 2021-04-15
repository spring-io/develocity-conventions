/*
 * Copyright 2020-2021 the original author or authors.
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

import java.io.File;

import org.gradle.api.Action;
import org.gradle.api.internal.ProcessOperations;
import org.gradle.process.ExecResult;
import org.gradle.process.ExecSpec;
import org.gradle.process.JavaExecSpec;

/**
 * {@link ProcessOperations} decorator that configures the working directory.
 *
 * @author Andy Wilkinson
 */
class WorkingDirectoryProcessOperations implements ProcessOperations {

	private final ProcessOperations delegate;

	private final File workingDir;

	WorkingDirectoryProcessOperations(ProcessOperations delegate, File workingDir) {
		this.delegate = delegate;
		this.workingDir = workingDir;
	}

	@Override
	public ExecResult exec(Action<? super ExecSpec> action) {
		return this.delegate.exec((spec) -> {
			spec.setWorkingDir(this.workingDir);
			action.execute(spec);
		});
	}

	@Override
	public ExecResult javaexec(Action<? super JavaExecSpec> action) {
		return this.delegate.javaexec((spec) -> {
			spec.setWorkingDir(this.workingDir);
			action.execute(spec);
		});
	}

}
