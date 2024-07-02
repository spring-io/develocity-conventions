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

import java.util.Map;

/**
 * Conventions that are applied to the build cache.
 *
 * @author Andy Wilkinson
 */
public class BuildCacheConventions {

	private final Map<String, String> env;

	public BuildCacheConventions() {
		this(System.getenv());
	}

	BuildCacheConventions(Map<String, String> env) {
		this.env = env;
	}

	/**
	 * Applies the conventions to the given {@code buildCache}.
	 * @param buildCache build cache to be configured
	 */
	public void execute(ConfigurableBuildCache buildCache) {
		buildCache.local((local) -> local.enable());
		buildCache.remote((remote) -> {
			remote.enable();
			String cacheServer = this.env.get("DEVELOCITY_CACHE_SERVER");
			if (cacheServer == null) {
				cacheServer = serverOfCacheUrl(this.env.get("GRADLE_ENTERPRISE_CACHE_URL"));
				if (cacheServer == null) {
					cacheServer = "https://ge.spring.io";
				}
			}
			remote.setServer(cacheServer);
			String accessKey = this.env.get("DEVELOCITY_ACCESS_KEY");
			if (accessKey == null) {
				accessKey = this.env.get("GRADLE_ENTERPRISE_ACCESS_KEY");
			}
			if (hasText(accessKey) && ContinuousIntegration.detect(this.env) != null) {
				remote.enablePush();
			}
		});
	}

	private String serverOfCacheUrl(String cacheUrl) {
		if (cacheUrl != null) {
			if (cacheUrl.endsWith("/cache/")) {
				return cacheUrl.substring(0, cacheUrl.length() - 7);
			}
			if (cacheUrl.endsWith("/cache")) {
				return cacheUrl.substring(0, cacheUrl.length() - 6);
			}
		}
		return null;
	}

	private boolean hasText(String string) {
		return string != null && string.length() > 0;
	}

}
