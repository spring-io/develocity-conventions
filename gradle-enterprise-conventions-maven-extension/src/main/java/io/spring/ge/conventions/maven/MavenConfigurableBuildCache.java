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

package io.spring.ge.conventions.maven;

import java.net.URI;
import java.util.function.Consumer;

import com.gradle.maven.extension.api.cache.BuildCacheApi;
import com.gradle.maven.extension.api.cache.Credentials;
import io.spring.ge.conventions.core.ConfigurableBuildCache;

/**
 * A {@link ConfigurableBuildCache} that configures the
 * {@link com.gradle.maven.extension.api.cache.LocalBuildCache LocalBuildCache} and
 * {@link com.gradle.maven.extension.api.cache.RemoteBuildCache RemoteBuildCache} for a
 * Maven build.
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

		private com.gradle.maven.extension.api.cache.LocalBuildCache local;

		MavenLocalBuildCache(com.gradle.maven.extension.api.cache.LocalBuildCache local) {
			this.local = local;
		}

		@Override
		public void enable() {
			this.local.setEnabled(true);
		}

	}

	private static final class MavenRemoteBuildCache implements RemoteBuildCache {

		private com.gradle.maven.extension.api.cache.RemoteBuildCache remote;

		private MavenRemoteBuildCache(com.gradle.maven.extension.api.cache.RemoteBuildCache remote) {
			this.remote = remote;
		}

		@Override
		public void enable() {
			this.remote.setEnabled(true);
		}

		@Override
		public void enablePush() {
			this.remote.setStoreEnabled(true);
		}

		@Override
		public void setUri(URI uri) {
			this.remote.getServer().setUrl(uri);
		}

		@Override
		public void setCredentials(String username, String password) {
			Credentials credentials = this.remote.getServer().getCredentials();
			credentials.setUsername(username);
			credentials.setPassword(password);
		}

	}

}
