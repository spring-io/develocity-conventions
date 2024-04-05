/*
 * Copyright 2024 the original author or authors.
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

import com.gradle.develocity.agent.gradle.DevelocityConfiguration;
import com.gradle.develocity.agent.gradle.buildcache.DevelocityBuildCache;
import com.gradle.develocity.agent.gradle.scan.BuildScanConfiguration;
import org.gradle.api.Action;
import org.gradle.api.provider.Property;

/**
 * {@link DevelocityConfiguration} implementation used for unit testing.
 *
 * @author Andy Wilkinson
 */
class TestDevelocityConfiguration implements DevelocityConfiguration {

	final TestProperty<String> server = new TestProperty<>();

	@Override
	public void buildScan(Action<? super BuildScanConfiguration> action) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Property<String> getAccessKey() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Property<Boolean> getAllowUntrustedServer() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Class<? extends DevelocityBuildCache> getBuildCache() {
		throw new UnsupportedOperationException();
	}

	@Override
	public BuildScanConfiguration getBuildScan() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Property<String> getProjectId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Property<String> getServer() {
		return this.server;

	}

}
