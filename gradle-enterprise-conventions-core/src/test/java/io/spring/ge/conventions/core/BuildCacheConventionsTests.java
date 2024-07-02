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

package io.spring.ge.conventions.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

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

	private final TestConfigurableBuildCache buildCache = new TestConfigurableBuildCache();

	@Test
	void localCacheIsEnabled() {
		new BuildCacheConventions().execute(this.buildCache);
		assertThat(this.buildCache.local.enabled).isTrue();
	}

	@Test
	void remoteCacheIsEnabled() {
		new BuildCacheConventions().execute(this.buildCache);
		assertThat(this.buildCache.remote.enabled).isTrue();
		assertThat(this.buildCache.remote.server).isEqualTo("https://ge.spring.io");
		assertThat(this.buildCache.remote.push).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "https://ge.example.com/cache/", "https://ge.example.com/cache" })
	void remoteCacheUrlCanBeConfigured(String cacheUrl) {
		Map<String, String> env = new HashMap<>();
		env.put("GRADLE_ENTERPRISE_CACHE_URL", cacheUrl);
		new BuildCacheConventions(env).execute(this.buildCache);
		assertThat(this.buildCache.remote.enabled).isTrue();
		assertThat(this.buildCache.remote.server).isEqualTo("https://ge.example.com");
		assertThat(this.buildCache.remote.push).isFalse();
	}

	@Test
	void remoteCacheServerCanBeConfigured() {
		Map<String, String> env = new HashMap<>();
		env.put("DEVELOCITY_CACHE_SERVER", "https://ge.example.com");
		new BuildCacheConventions(env).execute(this.buildCache);
		assertThat(this.buildCache.remote.enabled).isTrue();
		assertThat(this.buildCache.remote.server).isEqualTo("https://ge.example.com");
		assertThat(this.buildCache.remote.push).isFalse();
	}

	@Test
	void remoteCacheServerHasPrecedenceOverRemoteCacheUrl() {
		Map<String, String> env = new HashMap<>();
		env.put("GRADLE_ENTERPRISE_CACHE_URL", "https://ge-cache.example.com/cache/");
		env.put("DEVELOCITY_CACHE_SERVER", "https://ge.example.com");
		new BuildCacheConventions(env).execute(this.buildCache);
		assertThat(this.buildCache.remote.enabled).isTrue();
		assertThat(this.buildCache.remote.server).isEqualTo("https://ge.example.com");
		assertThat(this.buildCache.remote.push).isFalse();
	}

	@Test
	void whenAccessTokenIsProvidedInALocalEnvironmentThenPushingToTheRemoteCacheIsNotEnabled() {
		new BuildCacheConventions(Collections.singletonMap("DEVELOCITY_ACCESS_KEY", "ge.example.com=a1b2c3d4"))
			.execute(this.buildCache);
		assertThat(this.buildCache.remote.push).isFalse();
	}

	@Test
	void whenAccessTokenIsProvidedInACiEnvironmentThenPushingToTheRemoteCacheIsEnabled() {
		Map<String, String> env = new HashMap<>();
		env.put("DEVELOCITY_ACCESS_KEY", "ge.example.com=a1b2c3d4");
		env.put("CI", "true");
		new BuildCacheConventions(env).execute(this.buildCache);
		assertThat(this.buildCache.remote.push).isTrue();
	}

	@Test
	void whenLegacyAccessTokenIsProvidedInALocalEnvironmentThenPushingToTheRemoteCacheIsNotEnabled() {
		new BuildCacheConventions(Collections.singletonMap("GRADLE_ENTERPRISE_ACCESS_KEY", "ge.example.com=a1b2c3d4"))
			.execute(this.buildCache);
		assertThat(this.buildCache.remote.push).isFalse();
	}

	@Test
	void whenLegacyAccessTokenIsProvidedInACiEnvironmentThenPushingToTheRemoteCacheIsEnabled() {
		Map<String, String> env = new HashMap<>();
		env.put("GRADLE_ENTERPRISE_ACCESS_KEY", "ge.example.com=a1b2c3d4");
		env.put("CI", "true");
		new BuildCacheConventions(env).execute(this.buildCache);
		assertThat(this.buildCache.remote.push).isTrue();
	}

	private static final class TestConfigurableBuildCache implements ConfigurableBuildCache {

		private final TestLocalBuildCache local = new TestLocalBuildCache();

		private final TestRemoteBuildCache remote = new TestRemoteBuildCache();

		@Override
		public void local(Consumer<LocalBuildCache> local) {
			local.accept(this.local);
		}

		@Override
		public void remote(Consumer<RemoteBuildCache> remote) {
			remote.accept(this.remote);
		}

		private static final class TestLocalBuildCache implements LocalBuildCache {

			private boolean enabled = false;

			@Override
			public void enable() {
				this.enabled = true;
			}

		}

		private static final class TestRemoteBuildCache implements RemoteBuildCache {

			private boolean enabled = false;

			private boolean push = false;

			private String server = null;

			@Override
			public void enable() {
				this.enabled = true;
			}

			@Override
			public void enablePush() {
				this.push = true;
			}

			@Override
			public void setServer(String server) {
				this.server = server;
			}

		}

	}

}
