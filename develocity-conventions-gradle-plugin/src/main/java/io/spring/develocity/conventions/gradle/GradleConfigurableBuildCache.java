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

import java.util.function.Consumer;

import com.gradle.develocity.agent.gradle.buildcache.DevelocityBuildCache;
import io.spring.develocity.conventions.core.ConfigurableBuildCache;
import org.gradle.caching.configuration.BuildCacheConfiguration;
import org.gradle.caching.local.DirectoryBuildCache;

/**
 * A {@link ConfigurableBuildCache} for Gradle builds.
 *
 * @author Andy Wilkinson
 */
class GradleConfigurableBuildCache implements ConfigurableBuildCache {

	private final Class<? extends DevelocityBuildCache> buildCacheType;

	private final BuildCacheConfiguration buildCache;

	GradleConfigurableBuildCache(Class<? extends DevelocityBuildCache> buildCacheType,
			BuildCacheConfiguration buildCache) {
		this.buildCacheType = buildCacheType;
		this.buildCache = buildCache;
	}

	@Override
	public void local(Consumer<LocalBuildCache> local) {
		local.accept(new GradleLocalBuildCache(this.buildCache.getLocal()));
	}

	@Override
	public void remote(Consumer<RemoteBuildCache> remote) {
		remote.accept(new GradleRemoteBuildCache(this.buildCache.remote(this.buildCacheType)));
	}

	private static final class GradleLocalBuildCache implements LocalBuildCache {

		private final DirectoryBuildCache localBuildCache;

		private GradleLocalBuildCache(DirectoryBuildCache localBuildCache) {
			this.localBuildCache = localBuildCache;
		}

		@Override
		public void enable() {
			this.localBuildCache.setEnabled(true);
		}

	}

	private static final class GradleRemoteBuildCache implements RemoteBuildCache {

		private final DevelocityBuildCache remoteBuildCache;

		private GradleRemoteBuildCache(DevelocityBuildCache buildCache) {
			this.remoteBuildCache = buildCache;
		}

		@Override
		public void enable() {
			this.remoteBuildCache.setEnabled(true);
		}

		@Override
		public void enablePush() {
			this.remoteBuildCache.setPush(true);
		}

		@Override
		public void setServer(String server) {
			this.remoteBuildCache.setServer(server);
		}

	}

}
