package com.analyzer.runner;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.analyzer.core.ConfigFileLoader;

@Component
public class ConfigRunner implements CommandLineRunner {
	
	@Autowired
	ConfigFileLoader configFileLoader;

	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
		String folderPath = "samples";

        // Parse --path argument manually
        for (String arg : args) {
            if (arg.startsWith("--path=")) {
                folderPath = arg.substring("--path=".length());
                break;
            }
        }

        Path basePath = Paths.get(folderPath);
        if (!Files.exists(basePath) || !Files.isDirectory(basePath)) {
            System.err.println("Invalid folder path: " + basePath);
            return;
        }

        System.out.println("Scanning config files in: " + basePath.toAbsolutePath());

        Files.walk(basePath)
                .filter(Files::isRegularFile)
                .filter(path -> {
                    String name = path.getFileName().toString().toLowerCase();
                    return name.endsWith(".yml") || name.endsWith(".yaml") || name.endsWith(".properties");
                })
                .forEach(path -> {
                    File file = path.toFile();
                    System.out.println("\n Analyzing: " + file.getName());
                    try {
                        Map<String, Object> flattened = configFileLoader.load(file);
                        flattened.forEach((k, v) -> System.out.println(k + " = " + v));
                    } catch (Exception e) {
                        System.err.println("Failed to process " + file.getName() + ": " + e.getMessage());
                    }
                });

        System.out.println("\n Analysis complete.");

	}

}
