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

import java.net.InetAddress;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A build scan that can be configured. Provides a build-system agnostic API that can be
 * used with both Gradle and Maven builds.
 *
 * @author Andy Wilkinson
 */
public interface ConfigurableBuildScan {

	/**
	 * Configures whether to capture input files.
	 * @param capture {@code true} if input files for Gradle tasks or Maven goals should
	 * be captured, otherwise {@code false}.
	 */
	void captureInputFiles(boolean capture);

	/**
	 * Configures obfuscation of data in the build scan.
	 * @param configurer called to configure the obfuscation
	 */
	void obfuscation(Consumer<ObfuscationConfigurer> configurer);

	/**
	 * Configures the build scan to only be published when authenticated.
	 */
	void publishIfAuthenticated();

	/**
	 * Configures whether to upload the build scan in the background.
	 * @param enabled {@code true} to use background uploads, otherwise {@code false}.
	 */
	void uploadInBackground(boolean enabled);

	/**
	 * Adds a link with the given {@code name} and {@code url} to the build scan.
	 * @param name the name of the link
	 * @param url the URL of the link
	 */
	void link(String name, String url);

	/**
	 * Adds a tag to the build scan.
	 * @param tag the tag
	 */
	void tag(String tag);

	/**
	 * Adds a name-value pair to the build scan.
	 * @param name the name
	 * @param value the value
	 */
	void value(String name, String value);

	/**
	 * Configures the build scan in the background.
	 * @param backgroundConfigurer called in the background to configure the build scan
	 */
	void background(Consumer<ConfigurableBuildScan> backgroundConfigurer);

	/**
	 * Configures the obfuscation of data in the build scan.
	 */
	interface ObfuscationConfigurer {

		/**
		 * Obfuscates IP addresses in the build scan by applying the given
		 * {@code obfuscator} function to them.
		 * @param obfuscator function to obfuscate IP addresses
		 */
		void ipAddresses(Function<? super List<InetAddress>, ? extends List<String>> obfuscator);

	}

}
