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

package io.spring.develocity.conventions.core;

import java.util.function.Consumer;

/**
 * A build cache that can be configured. Provides a build-system agnostic API that can be
 * used with both Gradle and Maven builds.
 *
 * @author Andy Wilkinson
 */
public interface ConfigurableBuildCache {

	/**
	 * Configures the local build cache.
	 * @param local a consumer that is called to configure the {@link LocalBuildCache}
	 */
	void local(Consumer<LocalBuildCache> local);

	/**
	 * Configures the remote build cache.
	 * @param remote a consumer that is called to configure the {@link RemoteBuildCache}
	 */
	void remote(Consumer<RemoteBuildCache> remote);

	/**
	 * Configuration for the local build cache.
	 */
	interface LocalBuildCache {

		/**
		 * Enables the local build cache.
		 */
		void enable();

	}

	/**
	 * Configuration for the remote build cache.
	 */
	interface RemoteBuildCache {

		/**
		 * Enables the remote build cache.
		 */
		void enable();

		/**
		 * Enables the pushing of entries to the remote build cache.
		 */
		void enablePush();

		/**
		 * Sets the server that's hosting the remote build cache.
		 * @param server remote cache's server
		 */
		void setServer(String server);

	}

}
