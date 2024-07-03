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

package io.spring.develocity.conventions.maven;

import java.io.File;
import java.net.URI;

import com.gradle.develocity.agent.maven.api.cache.BuildCacheApi;
import com.gradle.develocity.agent.maven.api.cache.CleanupPolicy;
import com.gradle.develocity.agent.maven.api.cache.Credentials;
import com.gradle.develocity.agent.maven.api.cache.LocalBuildCache;
import com.gradle.develocity.agent.maven.api.cache.MojoMetadataProvider;
import com.gradle.develocity.agent.maven.api.cache.NormalizationProvider;
import com.gradle.develocity.agent.maven.api.cache.RemoteBuildCache;
import com.gradle.develocity.agent.maven.api.cache.Server;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MavenConfigurableBuildCache}.
 *
 * @author Andy Wilkinson
 */
class MavenConfigurableBuildCacheTests {

	private final BuildCacheApi buildCacheApi = new TestBuildCacheApi();

	private final MavenConfigurableBuildCache buildCache = new MavenConfigurableBuildCache(this.buildCacheApi);

	@Test
	void localBuildCacheCanBeEnabled() {
		this.buildCache.local((local) -> local.enable());
		assertThat(this.buildCacheApi.getLocal().isEnabled()).isTrue();
	}

	@Test
	void remoteBuildCacheCanBeEnabled() {
		this.buildCache.remote((remote) -> remote.enable());
		assertThat(this.buildCacheApi.getRemote().isEnabled()).isTrue();
	}

	@Test
	void remoteBuildCacheCanHavePushEnabled() {
		this.buildCache.remote((remote) -> remote.enablePush());
		assertThat(this.buildCacheApi.getRemote().isStoreEnabled()).isTrue();
	}

	@Test
	void remoteBuildCacheCanHaveItsServerConfigured() {
		this.buildCache.remote((remote) -> remote.setServer("https://ge.spring.io"));
		assertThat(this.buildCacheApi.getRemote().getServer().getUrl())
			.isEqualTo(URI.create("https://ge.spring.io/cache/"));
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

		@Override
		public boolean isRequireClean() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void registerMojoMetadataProvider(MojoMetadataProvider metadataProvider) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void registerNormalizationProvider(NormalizationProvider normalizationProvider) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setRequireClean(boolean requireClean) {
			throw new UnsupportedOperationException();
		}

		private static final class TestLocalBuildCache implements LocalBuildCache {

			private boolean enabled;

			private boolean storeEnabled;

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
			public boolean isStoreEnabled() {
				return this.storeEnabled;
			}

			@Override
			public void setDirectory(File directory) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void setEnabled(boolean enabled) {
				this.enabled = enabled;
			}

			@Override
			public void setStoreEnabled(boolean storeEnabled) {
				this.storeEnabled = storeEnabled;
			}

		}

		private static final class TestRemoteBuildCache implements RemoteBuildCache {

			private final Server server = new TestServer();

			private boolean enabled;

			private boolean storeEnabled;

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

			private static final class TestServer implements Server {

				private URI url;

				@Override
				public Credentials getCredentials() {
					throw new UnsupportedOperationException();
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
				public boolean isAllowInsecureProtocol() {
					throw new UnsupportedOperationException();
				}

				@Override
				public boolean isAllowUntrusted() {
					throw new UnsupportedOperationException();
				}

				@Override
				public boolean isUseExpectContinue() {
					throw new UnsupportedOperationException();
				}

				@Override
				public void setAllowInsecureProtocol(boolean allowInsecureProtocol) {
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

				@Override
				public void setUseExpectContinue(boolean useExpectContinue) {
					throw new UnsupportedOperationException();
				}

			}

		}

	}

}
