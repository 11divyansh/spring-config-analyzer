package com.analyzer.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class SpringMetadataLoader {

    public static Set<String> loadValidKeys() {
        Set<String> keys = new HashSet<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath*:META-INF/spring-configuration-metadata.json");

            for (Resource resource : resources) {
                try (InputStream in = resource.getInputStream()) {
                    JsonNode root = mapper.readTree(in);
                    if (root.has("properties")) {
                        for (JsonNode prop : root.get("properties")) {
                            String name = prop.get("name").asText();
                            keys.add(name);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return keys;
    }
}

