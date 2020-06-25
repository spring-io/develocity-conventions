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

package io.spring.ge.gradle;

import java.util.Map;

import org.gradle.api.Action;
import org.gradle.caching.configuration.BuildCache;
import org.gradle.caching.configuration.BuildCacheConfiguration;
import org.gradle.caching.http.HttpBuildCache;
import org.gradle.caching.http.HttpBuildCacheCredentials;

/**
 * {@link Action} that configures the {@link BuildCache build cache} with Spring
 * conventions.
 *
 * @author Andy Wilkinson
 */
class BuildCacheConventions implements Action<BuildCacheConfiguration> {

	private final Map<String, String> env;

	BuildCacheConventions() {
		this(System.getenv());
	}

	BuildCacheConventions(Map<String, String> env) {
		this.env = env;
	}

	@Override
	public void execute(BuildCacheConfiguration buildCache) {
		buildCache.getLocal().setEnabled(true);
		HttpBuildCache httpCache = buildCache.remote(HttpBuildCache.class);
		httpCache.setEnabled(true);
		httpCache.setUrl("https://ge.spring.io/cache/");
		String username = this.env.get("GRADLE_ENTERPRISE_CACHE_USERNAME");
		String password = this.env.get("GRADLE_ENTERPRISE_CACHE_PASSWORD");
		if (hasText(username) && hasText(password)) {
			httpCache.setPush(true);
			HttpBuildCacheCredentials credentials = httpCache.getCredentials();
			credentials.setUsername(username);
			credentials.setPassword(password);
		}
	}

	private boolean hasText(String string) {
		return string != null && string.length() > 0;
	}

}
