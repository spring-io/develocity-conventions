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

package io.spring.ge.conventions.core;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

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
		assertThat(this.buildCache.remote.uri).isEqualTo(URI.create("https://ge.spring.io/cache/"));
		assertThat(this.buildCache.remote.push).isFalse();
	}

	@Test
	void whenCredentialsAreProvidedThenPushingToTheRemoteCacheIsEnabled() {
		Map<String, String> env = new HashMap<String, String>();
		env.put("GRADLE_ENTERPRISE_CACHE_USERNAME", "user");
		env.put("GRADLE_ENTERPRISE_CACHE_PASSWORD", "secret");
		new BuildCacheConventions(env).execute(this.buildCache);
		assertThat(this.buildCache.remote.push).isTrue();
		assertThat(this.buildCache.remote.username).isEqualTo("user");
		assertThat(this.buildCache.remote.password).isEqualTo("secret");
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

			private URI uri = null;

			private String username = null;

			private String password = null;

			@Override
			public void enable() {
				this.enabled = true;
			}

			@Override
			public void enablePush() {
				this.push = true;
			}

			@Override
			public void setUri(URI uri) {
				this.uri = uri;
			}

			@Override
			public void setCredentials(String username, String password) {
				this.username = username;
				this.password = password;
			}

		}

	}

}
