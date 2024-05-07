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

package io.spring.ge.conventions.gradle;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.gradle.develocity.agent.gradle.buildcache.DevelocityBuildCache;
import org.gradle.api.Action;
import org.gradle.caching.BuildCacheServiceFactory;
import org.gradle.caching.configuration.BuildCache;
import org.gradle.caching.configuration.BuildCacheConfiguration;
import org.gradle.caching.local.DirectoryBuildCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BuildCacheConventions}.
 *
 * @author Andy Wilkinson
 */
class BuildCacheConventionsTests {

	private final TestBuildCacheConfiguration buildCache = new TestBuildCacheConfiguration();

	@Test
	void localCacheIsEnabled() {
		new BuildCacheConventions(DevelocityBuildCache.class).execute(this.buildCache);
		assertThat(this.buildCache.local.isEnabled()).isTrue();
	}

	@Test
	void remoteCacheIsEnabled() {
		new BuildCacheConventions(DevelocityBuildCache.class).execute(this.buildCache);
		assertThat(this.buildCache.remote.isEnabled()).isTrue();
		assertThat(this.buildCache.remote.getServer()).isNull();
		assertThat(this.buildCache.remote.isPush()).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "https://ge.example.com/cache/", "https://ge.example.com/cache" })
	void remoteCacheUrlCanBeConfigured(String cacheUrl) {
		Map<String, String> env = new HashMap<>();
		env.put("GRADLE_ENTERPRISE_CACHE_URL", cacheUrl);
		new BuildCacheConventions(DevelocityBuildCache.class, env).execute(this.buildCache);
		assertThat(this.buildCache.remote.isEnabled()).isTrue();
		assertThat(this.buildCache.remote.getServer()).isEqualTo("https://ge.example.com");
		assertThat(this.buildCache.remote.isPush()).isFalse();
	}

	@Test
	void remoteCacheServerCanBeConfigured() {
		Map<String, String> env = new HashMap<>();
		env.put("DEVELOCITY_CACHE_SERVER", "https://ge.example.com");
		new BuildCacheConventions(DevelocityBuildCache.class, env).execute(this.buildCache);
		assertThat(this.buildCache.remote.isEnabled()).isTrue();
		assertThat(this.buildCache.remote.getServer()).isEqualTo("https://ge.example.com");
		assertThat(this.buildCache.remote.isPush()).isFalse();
	}

	@Test
	void remoteCacheServerHasPrecedenceOverRemoteCacheUrl() {
		Map<String, String> env = new HashMap<>();
		env.put("GRADLE_ENTERPRISE_CACHE_URL", "https://ge-cache.example.com/cache/");
		env.put("DEVELOCITY_CACHE_SERVER", "https://ge.example.com");
		new BuildCacheConventions(DevelocityBuildCache.class, env).execute(this.buildCache);
		assertThat(this.buildCache.remote.isEnabled()).isTrue();
		assertThat(this.buildCache.remote.getServer()).isEqualTo("https://ge.example.com");
		assertThat(this.buildCache.remote.isPush()).isFalse();
	}

	@Test
	void whenAccessTokenIsProvidedInALocalEnvironmentThenPushingToTheRemoteCacheIsNotEnabled() {
		new BuildCacheConventions(DevelocityBuildCache.class,
				Collections.singletonMap("DEVELOCITY_ACCESS_KEY", "ge.example.com=a1b2c3d4"))
			.execute(this.buildCache);
		assertThat(this.buildCache.remote.isPush()).isFalse();
	}

	@Test
	void whenAccessTokenIsProvidedInACiEnvironmentThenPushingToTheRemoteCacheIsEnabled() {
		Map<String, String> env = new HashMap<>();
		env.put("DEVELOCITY_ACCESS_KEY", "ge.example.com=a1b2c3d4");
		env.put("CI", "true");
		new BuildCacheConventions(DevelocityBuildCache.class, env).execute(this.buildCache);
		assertThat(this.buildCache.remote.isPush()).isTrue();
	}

	@Test
	void whenLegacyAccessTokenIsProvidedInALocalEnvironmentThenPushingToTheRemoteCacheIsNotEnabled() {
		new BuildCacheConventions(DevelocityBuildCache.class,
				Collections.singletonMap("GRADLE_ENTERPRISE_ACCESS_KEY", "ge.example.com=a1b2c3d4"))
			.execute(this.buildCache);
		assertThat(this.buildCache.remote.isPush()).isFalse();
	}

	@Test
	void whenLegacyAccessTokenIsProvidedInACiEnvironmentThenPushingToTheRemoteCacheIsEnabled() {
		Map<String, String> env = new HashMap<>();
		env.put("GRADLE_ENTERPRISE_ACCESS_KEY", "ge.example.com=a1b2c3d4");
		env.put("CI", "true");
		new BuildCacheConventions(DevelocityBuildCache.class, env).execute(this.buildCache);
		assertThat(this.buildCache.remote.isPush()).isTrue();
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
