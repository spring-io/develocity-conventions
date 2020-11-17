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

import java.io.File;
import java.net.URI;

import com.gradle.maven.extension.api.cache.BuildCacheApi;
import com.gradle.maven.extension.api.cache.CleanupPolicy;
import com.gradle.maven.extension.api.cache.Credentials;
import com.gradle.maven.extension.api.cache.LocalBuildCache;
import com.gradle.maven.extension.api.cache.RemoteBuildCache;
import com.gradle.maven.extension.api.cache.Server;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MavenConfigurableBuildCache}.
 *
 * @author Andy Wilkinson
 */
public class MavenConfigurableBuildCacheTests {

	private final TestBuildCacheApi api = new TestBuildCacheApi();

	private final MavenConfigurableBuildCache buildCache = new MavenConfigurableBuildCache(this.api);

	@Test
	void localCacheCanBeEnabled() {
		this.buildCache.local((local) -> local.enable());
		assertThat(this.api.getLocal().isEnabled()).isTrue();
	}

	@Test
	void remoteCacheCanBeEnabled() {
		this.buildCache.remote((remote) -> remote.enable());
		assertThat(this.api.getRemote().isEnabled()).isTrue();
	}

	@Test
	void pushingToRemoteCacheCanBeEnabled() {
		this.buildCache.remote((remote) -> remote.enablePush());
		assertThat(this.api.getRemote().isStoreEnabled()).isTrue();
	}

	@Test
	void remoteCacheUriCanBeConfigured() {
		this.buildCache.remote((remote) -> remote.setUri(URI.create("https://cache.example.com/")));
		assertThat(this.api.getRemote().getServer().getUrl()).isEqualTo(URI.create("https://cache.example.com/"));
	}

	@Test
	void remoteCacheCredentialsCanBeConfigured() {
		this.buildCache.remote((remote) -> remote.setCredentials("alice", "secret"));
		assertThat(this.api.getRemote().getServer().getCredentials().getUsername()).isEqualTo("alice");
		assertThat(this.api.getRemote().getServer().getCredentials().getPassword()).isEqualTo("secret");
	}

	private static final class TestBuildCacheApi implements BuildCacheApi {

		private final LocalBuildCache local = new TestLocalBuildCache();

		private final RemoteBuildCache remote = new TestRemoteBuildCache();

		@Override
		public LocalBuildCache getLocal() {
			return this.local;
		}

		@Override
		public RemoteBuildCache getRemote() {
			return this.remote;
		}

		private static final class TestLocalBuildCache implements LocalBuildCache {

			private boolean enabled = false;

			@Override
			public CleanupPolicy getCleanupPolicy() {
				throw new UnsupportedOperationException();
			}

			@Override
			public File getDirectory() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isEnabled() {
				return this.enabled;
			}

			@Override
			public void setDirectory(File directory) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void setEnabled(boolean enabled) {
				this.enabled = enabled;
			}

		}

		private static final class TestRemoteBuildCache implements RemoteBuildCache {

			private final Server server = new TestServer();

			private boolean enabled = false;

			private boolean storeEnabled = false;

			@Override
			public Server getServer() {
				return this.server;
			}

			@Override
			public boolean isEnabled() {
				return this.enabled;
			}

			@Override
			public boolean isStoreEnabled() {
				return this.storeEnabled;
			}

			@Override
			public void setEnabled(boolean enabled) {
				this.enabled = enabled;
			}

			@Override
			public void setStoreEnabled(boolean storeEnabled) {
				this.storeEnabled = storeEnabled;
			}

			private static class TestServer implements Server {

				private final Credentials credentials = new TestCredentials();

				private URI url;

				@Override
				public Credentials getCredentials() {
					return this.credentials;
				}

				@Override
				public String getServerId() {
					throw new UnsupportedOperationException();
				}

				@Override
				public URI getUrl() {
					return this.url;
				}

				@Override
				public boolean isAllowUntrusted() {
					throw new UnsupportedOperationException();
				}

				@Override
				public void setAllowUntrusted(boolean allowUntrusted) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void setServerId(String serverId) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void setUrl(URI url) {
					this.url = url;
				}

				private static class TestCredentials implements Credentials {

					private String username;

					private String password;

					@Override
					public String getUsername() {
						return this.username;
					}

					@Override
					public void setUsername(String username) {
						this.username = username;
					}

					@Override
					public String getPassword() {
						return this.password;
					}

					@Override
					public void setPassword(String password) {
						this.password = password;
					}

				}

			}

		}

	}

}
