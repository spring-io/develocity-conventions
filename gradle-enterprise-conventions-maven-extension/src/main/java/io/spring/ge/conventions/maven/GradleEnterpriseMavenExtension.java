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

package io.spring.ge.conventions.maven;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.gradle.maven.extension.api.cache.BuildCacheApi;
import com.gradle.maven.extension.api.scan.BuildScanApi;
import io.spring.ge.conventions.core.BuildCacheConventions;
import io.spring.ge.conventions.core.BuildScanConventions;
import io.spring.ge.conventions.core.ProcessRunner;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AbstractMavenLifecycleParticipant Maven extension} for configuring the use of
 * Gradle Enterprise hosted at <a href="https://ge.spring.io">ge.spring.io</a>.
 *
 * @author Andy Wilkinson
 */
@Component(role = AbstractMavenLifecycleParticipant.class)
public final class GradleEnterpriseMavenExtension extends AbstractMavenLifecycleParticipant {

	private static final Logger log = LoggerFactory.getLogger(GradleEnterpriseMavenExtension.class);

	private final PlexusContainer container;

	@Inject
	public GradleEnterpriseMavenExtension(PlexusContainer container) {
		this.container = container;
	}

	@Override
	public void afterSessionStart(MavenSession session) throws MavenExecutionException {
		log.debug("Executing extension: {}", getClass().getSimpleName());
		ApiAccessor apiAccessor = new ApiAccessor(getClass().getClassLoader(), this.container);
		BuildScanApi buildScan = apiAccessor.lookUpBuildScanApi();
		if (buildScan != null) {
			log.debug("Applying build scan conventions");
			new BuildScanConventions(new ProcessBuilderProcessRunner())
					.execute(new MavenConfigurableBuildScan(buildScan));
			log.debug("Build scan conventions applied");
		}
		BuildCacheApi buildCache = apiAccessor.lookUpBuildCacheApi();
		if (buildCache != null) {
			log.debug("Applying build cache conventions");
			new BuildCacheConventions().execute(new MavenConfigurableBuildCache(buildCache));
			log.debug("Build cache conventions applied");
		}
	}

	private static final class ProcessBuilderProcessRunner implements ProcessRunner {

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
				throw new RuntimeException("Process failed", ex);
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

}
