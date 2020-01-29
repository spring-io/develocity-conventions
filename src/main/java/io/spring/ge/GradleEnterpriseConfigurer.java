package io.spring.ge;

import java.util.stream.Collectors;

import org.gradle.api.GradleException;
import org.gradle.api.initialization.Settings;

import com.gradle.enterprise.gradleplugin.GradleEnterpriseExtension;
import com.gradle.enterprise.gradleplugin.GradleEnterprisePlugin;
import com.gradle.scan.plugin.BuildScanExtension;

public class GradleEnterpriseConfigurer {
	
	void configure(Settings settings) {
		settings.getPlugins().withType(GradleEnterprisePlugin.class, (plugin) -> {
			GradleEnterpriseExtension extension = settings.getExtensions().getByType(GradleEnterpriseExtension.class);
			extension.buildScan(this::configure);
		});
	}
	
	private void configure(BuildScanExtension buildScan) {
		buildScan.setCaptureTaskInputFiles(true);
		buildScan.obfuscation((obfuscation) -> obfuscation.ipAddresses((addresses) -> addresses.stream().map((address) -> "0.0.0.0").collect(Collectors.toList())));
		buildScan.publishAlways();
		try {
			buildScan.getClass().getMethod("publishIfAuthenticated").invoke(buildScan);
		} catch (Exception ex) {
			throw new GradleException("Failed to enable publishIfAuthenticated", ex);
		}
		buildScan.setServer("https://ge.spring.io");
	}

}
