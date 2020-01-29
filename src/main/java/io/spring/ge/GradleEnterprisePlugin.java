package io.spring.ge;

import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;

public class GradleEnterprisePlugin implements Plugin<Settings> {

	@Override
	public void apply(Settings settings) {
		new GradleEnterpriseConfigurer().configure(settings);
	}

}
