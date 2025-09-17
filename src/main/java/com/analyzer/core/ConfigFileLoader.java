package com.analyzer.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.springframework.stereotype.Component;

@Component
public class ConfigFileLoader {

    // Existing method
    public Map<String, Object> load(File file) throws IOException {
        String name = file.getName();
        if (name.endsWith(".yml") || name.endsWith(".yaml")) {
            return loadYaml(file);
        } else if (name.endsWith(".properties")) {
            return loadProperties(file);
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + name);
        }
    }

    // New method to read from InputStream (for resources inside JAR)
    public Map<String, Object> load(InputStream input, String filename) throws IOException {
        if (filename.endsWith(".yml") || filename.endsWith(".yaml")) {
            return loadYaml(input);
        } else if (filename.endsWith(".properties")) {
            return loadProperties(input);
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + filename);
        }
    }

    // Existing YAML loader for File
    private Map<String, Object> loadYaml(File file) throws IOException {
        try (InputStream input = new FileInputStream(file)) {
            return loadYaml(input);
        }
    }

    // New YAML loader for InputStream
    private Map<String, Object> loadYaml(InputStream input) throws IOException {
        LoadSettings settings = LoadSettings.builder().build();
        Load load = new Load(settings);
        Object data = load.loadFromInputStream(input);
        if (!(data instanceof Map)) return Collections.emptyMap();

        Map<String, Object> flatMap = new LinkedHashMap<>();
        flattenMap("", (Map<?, ?>) data, flatMap);
        return flatMap;
    }

    // Existing Properties loader for File
    private Map<String, Object> loadProperties(File file) throws IOException {
        try (InputStream input = new FileInputStream(file)) {
            return loadProperties(input);
        }
    }

    // New Properties loader for InputStream
    private Map<String, Object> loadProperties(InputStream input) throws IOException {
        Properties props = new Properties();
        props.load(input);
        return props.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> String.valueOf(e.getKey()),
                        Map.Entry::getValue,
                        (a, b) -> b,
                        LinkedHashMap::new
                ));
    }

    // Flatten map (unchanged)
    private void flattenMap(String prefix, Map<?, ?> source, Map<String, Object> target) {
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            if (value instanceof Map) {
                flattenMap(fullKey, (Map<?, ?>) value, target);
            } else {
                target.put(fullKey, value);
            }
        }
    }
}
