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

import com.gradle.develocity.agent.gradle.buildcache.DevelocityBuildCache;
import org.gradle.api.Action;
import org.gradle.caching.BuildCacheServiceFactory;
import org.gradle.caching.configuration.BuildCache;
import org.gradle.caching.configuration.BuildCacheConfiguration;
import org.gradle.caching.local.DirectoryBuildCache;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link GradleConfigurableBuildCache}.
 *
 * @author Andy Wilkinson
 */
class GradleConfigurableBuildCacheTests {

	private final TestBuildCacheConfiguration buildCache = new TestBuildCacheConfiguration();

	@Test
	void localCacheCanBeEnabled() {
		new GradleConfigurableBuildCache(DevelocityBuildCache.class, this.buildCache).local((local) -> local.enable());
		assertThat(this.buildCache.local.isEnabled()).isTrue();
	}

	@Test
	void remoteCacheCanBeEnabled() {
		new GradleConfigurableBuildCache(DevelocityBuildCache.class, this.buildCache)
			.remote((remote) -> remote.enable());
	}

	@Test
	void pushToRemoteCacheCanBeEnabled() {
		new GradleConfigurableBuildCache(DevelocityBuildCache.class, this.buildCache)
			.remote((remote) -> remote.enablePush());
		assertThat(this.buildCache.remote.isPush()).isTrue();
	}

	@Test
	void remoteServerCanBeConfigured() {
		new GradleConfigurableBuildCache(DevelocityBuildCache.class, this.buildCache)
			.remote((remote) -> remote.setServer("https://ge.spring.io"));
		assertThat(this.buildCache.remote.getServer()).isEqualTo("https://ge.spring.io");
	}

	private static final class TestBuildCacheConfiguration implements BuildCacheConfiguration {

		private final DirectoryBuildCache local = new DirectoryBuildCache();

		private final DevelocityBuildCache remote = new DevelocityBuildCache() {
		};

		@Override
		public DirectoryBuildCache getLocal() {
			return this.local;
		}

		@Override
		public BuildCache getRemote() {
			throw new UnsupportedOperationException();
		}

		@Override
		@Deprecated
		public <T extends DirectoryBuildCache> T local(Class<T> cacheType) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void local(Action<? super DirectoryBuildCache> action) {
			action.execute(this.local);
		}

		@Override
		@Deprecated
		public <T extends DirectoryBuildCache> T local(Class<T> cacheType, Action<? super T> action) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T extends BuildCache> void registerBuildCacheService(Class<T> cacheType,
				Class<? extends BuildCacheServiceFactory<? super T>> factory) {
			throw new UnsupportedOperationException();
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T extends BuildCache> T remote(Class<T> cacheType) {
			return (T) this.remote;
		}

		@Override
		public void remote(Action<? super BuildCache> action) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T extends BuildCache> T remote(Class<T> type, Action<? super T> action) {
			T cache = remote(type);
			action.execute(cache);
			return cache;
		}

	}

}
