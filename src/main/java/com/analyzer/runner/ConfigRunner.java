package com.analyzer.runner;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import com.analyzer.core.AnnotationKeyExtractor;
import com.analyzer.core.ConfigFileLoader;


@Component
public class ConfigRunner implements CommandLineRunner {
	
	@Autowired
	ConfigFileLoader configFileLoader;

	@Override
	public void run(String... args) throws Exception {
		Map<String, Object> flattened=null;
	    System.out.println("Scanning config files in resources...");
	    
	    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

	    // Scan all .yml, .yaml, .properties files in resources
	    Resource[] resources = resolver.getResources("classpath*:*.{properties,yml,yaml}");

	    for (Resource resource : resources) {
	    	String filename = resource.getFilename().toLowerCase();
	    	if (filename.startsWith("log4j") || filename.startsWith("module-info") || filename.equals("LICENSE-ClassGraph.txt")) {
	    	    continue;
	    	}
	        if (!(filename.endsWith(".yml") || filename.endsWith(".yaml") || filename.endsWith(".properties"))) {
	            continue; // skip unsupported files
	        }
	        System.out.println("\nAnalyzing: " + resource.getFilename());
	        try {
	            // If your ConfigFileLoader.load supports File
	            if (resource.isFile()) {
	                flattened = configFileLoader.load(resource.getFile());
	                flattened.forEach((k, v) -> System.out.println(k + " = " + v));
	            } else {
	                // Fallback for resources inside JAR
	                try (InputStream in = resource.getInputStream()) {
	                	 flattened = configFileLoader.load(in, resource.getFilename());
	                	flattened.forEach((k, v) -> System.out.println(k + " = " + v));
	                }
	                
	            }
	        } catch (Exception e) {
	            System.err.println("Failed to process " + resource.getFilename() + ": " + e.getMessage());
	        }
	    }

	    System.out.println("\nAnalysis complete.");

	    AnnotationKeyExtractor extractor = new AnnotationKeyExtractor();
	    Set<String> usedKeys = extractor.extractKeys("com.analyzer.model");

	    System.out.println("=== Referenced Keys ===");
	    usedKeys.forEach(System.out::println);
	    
	    Set<String> deadKeys = new HashSet<>(flattened.keySet());
	    deadKeys.removeAll(usedKeys);
	    
	    if (deadKeys.isEmpty()) {
	        System.out.println("No dead config keys found.");
	    } else {
	        System.out.println("=== Dead / Unused Config Keys ===");
	        deadKeys.forEach(System.out::println);
	    }
	}

}
