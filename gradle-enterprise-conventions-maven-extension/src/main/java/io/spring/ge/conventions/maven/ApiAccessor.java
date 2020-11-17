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

import java.util.Comparator;
import java.util.Optional;

import com.gradle.maven.extension.api.cache.BuildCacheApi;
import com.gradle.maven.extension.api.scan.BuildScanApi;
import org.apache.maven.MavenExecutionException;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 * Provides access to the {@link BuildCacheApi} and {@link BuildScanApi}, working around
 * <a href="https://issues.apache.org/jira/browse/MNG-6906">MNG-6906</a>. Adapted from
 * <a href=
 * "https://github.com/gradle/gradle-enterprise-build-config-samples/blob/1f462638fa8d6f0ec2e6a14c5eb1ae445f55c8af/common-custom-user-data-maven-extension/src/main/java/com/gradle/ApiAccessor.java">ApiAccessor.java
 * in Gradle's sample</a>.
 *
 * @author Andy Wilkinson
 */
class ApiAccessor {

	private static final String BUILD_CACHE_API_PACKAGE = "com.gradle.maven.extension.api.cache";

	private static final String BUILD_CACHE_API_CONTAINER_OBJECT = BUILD_CACHE_API_PACKAGE + ".BuildCacheApi";

	private static final String BUILD_SCAN_API_PACKAGE = "com.gradle.maven.extension.api.scan";

	private static final String BUILD_SCAN_API_CONTAINER_OBJECT = BUILD_SCAN_API_PACKAGE + ".BuildScanApi";

	private final ClassLoader classLoader;

	private final PlexusContainer container;

	ApiAccessor(ClassLoader classLoader, PlexusContainer container) {
		this.classLoader = classLoader;
		this.container = container;
		ensureClassIsAccessible(BUILD_CACHE_API_PACKAGE);
		ensureClassIsAccessible(BUILD_SCAN_API_PACKAGE);
	}

	BuildScanApi lookUpBuildScanApi() throws MavenExecutionException {
		return lookUpApi(BuildScanApi.class, BUILD_SCAN_API_PACKAGE, BUILD_SCAN_API_CONTAINER_OBJECT);
	}

	BuildCacheApi lookUpBuildCacheApi() throws MavenExecutionException {
		return lookUpApi(BuildCacheApi.class, BUILD_CACHE_API_PACKAGE, BUILD_CACHE_API_CONTAINER_OBJECT);
	}

	private <T> T lookUpApi(Class<T> api, String componentPackage, String containerObject)
			throws MavenExecutionException {
		return componentExists(containerObject) ? lookUpClass(api) : null;
	}

	// Workaround for https://issues.apache.org/jira/browse/MNG-6906
	@SuppressWarnings("resource")
	private void ensureClassIsAccessible(String componentPackage) {
		if (this.classLoader instanceof ClassRealm) {
			ClassRealm extensionRealm = (ClassRealm) this.classLoader;
			if (!"maven.ext".equals(extensionRealm.getId())) {
				Optional<ClassRealm> sourceRealm = extensionRealm.getWorld().getRealms().stream()
						.filter((realm) -> realm.getId().contains("com.gradle:gradle-enterprise-maven-extension")
								|| realm.getId().equals("maven.ext"))
						.max(Comparator.comparing((ClassRealm realm) -> realm.getId().length()));
				if (sourceRealm.isPresent()) {
					String sourceRealmId = sourceRealm.get().getId();
					try {
						extensionRealm.importFrom(sourceRealmId, componentPackage);
					}
					catch (Exception ex) {
						throw new IllegalStateException("Could not import package from realm with id " + sourceRealmId,
								ex);
					}
				}
			}
		}
	}

	private boolean componentExists(String component) {
		return this.container.hasComponent(component);
	}

	private <T> T lookUpClass(Class<T> componentClass) throws MavenExecutionException {
		try {
			return this.container.lookup(componentClass);
		}
		catch (ComponentLookupException ex) {
			throw new MavenExecutionException("Cannot look up object in container: " + componentClass.getName(), ex);
		}
	}

}
