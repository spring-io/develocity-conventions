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

import com.gradle.maven.extension.api.scan.BuildScanApi;
import org.apache.maven.MavenExecutionException;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 * Provides access to the {@link BuildScanApi}, working around
 * <a href="https://issues.apache.org/jira/browse/MNG-6906">MNG-6906</a>. Adapted from
 * <a href=
 * "https://github.com/gradle/gradle-enterprise-build-config-samples/blob/f05c84f84b5084f796d6935b606dfcbc5a5ca07c/common-custom-user-data-maven-extension/src/main/java/com/gradle/BuildScanApiAccessor.java">BuildScanApiAccessor.java
 * in Gradle's sample</a>.
 *
 * @author Andy Wilkinson
 */
final class BuildScanApiAccessor {

	private static final String PACKAGE = "com.gradle.maven.extension.api.scan";

	private static final String BUILD_SCAN_API_CONTAINER_OBJECT = PACKAGE + ".BuildScanApi";

	private BuildScanApiAccessor() {
	}

	static BuildScanApi lookup(PlexusContainer container, Class<?> extensionClass) throws MavenExecutionException {
		ensureBuildScanApiIsAccessible(extensionClass);
		try {
			return (BuildScanApi) container.lookup(BUILD_SCAN_API_CONTAINER_OBJECT);
		}
		catch (ComponentLookupException ex) {
			throw new MavenExecutionException("Cannot look up object in container: " + BUILD_SCAN_API_CONTAINER_OBJECT,
					ex);
		}
	}

	// Workaround for https://issues.apache.org/jira/browse/MNG-6906
	@SuppressWarnings("resource")
	private static void ensureBuildScanApiIsAccessible(Class<?> extensionClass) throws MavenExecutionException {
		ClassLoader classLoader = extensionClass.getClassLoader();
		if (classLoader instanceof ClassRealm) {
			ClassRealm extensionRealm = (ClassRealm) classLoader;
			if (!"maven.ext".equals(extensionRealm.getId())) {
				Optional<ClassRealm> sourceRealm = extensionRealm.getWorld().getRealms().stream()
						.filter((realm) -> realm.getId().contains("com.gradle:gradle-enterprise-maven-extension")
								|| realm.getId().equals("maven.ext"))
						.max(Comparator.comparing((ClassRealm realm) -> realm.getId().length()));
				if (sourceRealm.isPresent()) {
					String sourceRealmId = sourceRealm.get().getId();
					try {
						extensionRealm.importFrom(sourceRealmId, PACKAGE);
					}
					catch (Exception ex) {
						throw new MavenExecutionException(
								"Could not import package from realm with id " + sourceRealmId, ex);
					}
				}
			}
		}
	}

}
