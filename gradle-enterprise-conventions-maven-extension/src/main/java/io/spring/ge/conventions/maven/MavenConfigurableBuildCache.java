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

package io.spring.ge.conventions.maven;

import java.util.function.Consumer;

import com.gradle.develocity.agent.maven.api.cache.BuildCacheApi;
import io.spring.ge.conventions.core.ConfigurableBuildCache;

/**
 * {@link ConfigurableBuildCache} for Maven builds.
 *
 * @author Andy Wilkinson
 */
class MavenConfigurableBuildCache implements ConfigurableBuildCache {

	private final BuildCacheApi buildCache;

	MavenConfigurableBuildCache(BuildCacheApi buildCache) {
		this.buildCache = buildCache;
	}

	@Override
	public void local(Consumer<LocalBuildCache> local) {
		local.accept(new MavenLocalBuildCache(this.buildCache.getLocal()));
	}

	@Override
	public void remote(Consumer<RemoteBuildCache> remote) {
		remote.accept(new MavenRemoteBuildCache(this.buildCache.getRemote()));
	}

	private static final class MavenLocalBuildCache implements LocalBuildCache {

		private final com.gradle.develocity.agent.maven.api.cache.LocalBuildCache localBuildCache;

		private MavenLocalBuildCache(com.gradle.develocity.agent.maven.api.cache.LocalBuildCache localBuildCache) {
			this.localBuildCache = localBuildCache;
		}

		@Override
		public void enable() {
			this.localBuildCache.setEnabled(true);
		}

	}

	private static final class MavenRemoteBuildCache implements RemoteBuildCache {

		private final com.gradle.develocity.agent.maven.api.cache.RemoteBuildCache remoteBuildCache;

		private MavenRemoteBuildCache(com.gradle.develocity.agent.maven.api.cache.RemoteBuildCache remoteBuildCache) {
			this.remoteBuildCache = remoteBuildCache;
		}

		@Override
		public void enable() {
			this.remoteBuildCache.setEnabled(true);
		}

		@Override
		public void enablePush() {
			this.remoteBuildCache.setStoreEnabled(true);
		}

		@Override
		public void setServer(String server) {
			this.remoteBuildCache.getServer().setUrl(server);
		}

	}

}
