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

package io.spring.ge.conventions.gradle;

import java.net.URI;
import java.util.function.Consumer;

import io.spring.ge.conventions.core.ConfigurableBuildCache;
import org.gradle.caching.configuration.BuildCacheConfiguration;
import org.gradle.caching.http.HttpBuildCache;
import org.gradle.caching.http.HttpBuildCacheCredentials;
import org.gradle.caching.local.DirectoryBuildCache;

/**
 * A {@link ConfigurableBuildCache} that configures a {@link BuildCacheConfiguration} for
 * a Gradle build.
 *
 * @author Andy Wilkinson
 */
class GradleConfigurableBuildCache implements ConfigurableBuildCache {

	private final BuildCacheConfiguration configuration;

	GradleConfigurableBuildCache(BuildCacheConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void local(Consumer<LocalBuildCache> local) {
		local.accept(new GradleLocalBuildCache(this.configuration.getLocal()));
	}

	@Override
	public void remote(Consumer<RemoteBuildCache> remote) {
		remote.accept(new GradleRemoteBuildCache(this.configuration.remote(HttpBuildCache.class)));
	}

	static class GradleLocalBuildCache implements LocalBuildCache {

		private final DirectoryBuildCache cache;

		GradleLocalBuildCache(DirectoryBuildCache cache) {
			this.cache = cache;
		}

		@Override
		public void enable() {
			this.cache.setEnabled(true);
		}

	}

	static class GradleRemoteBuildCache implements RemoteBuildCache {

		private final HttpBuildCache cache;

		GradleRemoteBuildCache(HttpBuildCache cache) {
			this.cache = cache;
		}

		@Override
		public void enable() {
			this.cache.setEnabled(true);
		}

		@Override
		public void enablePush() {
			this.cache.setPush(true);
		}

		@Override
		public void setUri(URI uri) {
			this.cache.setUrl(uri);
		}

		@Override
		public void setCredentials(String username, String password) {
			HttpBuildCacheCredentials credentials = this.cache.getCredentials();
			credentials.setUsername(username);
			credentials.setPassword(password);
		}

	}

}
