package com.analyzer.core;

import java.util.HashSet;
import java.util.Set;

import io.github.classgraph.ClassInfo;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.ScanResult;

public class AnnotationKeyExtractor {

    public Set<String> extractKeys(String basePackage) {
        Set<String> referencedKeys = new HashSet<>();

        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .acceptPackages(basePackage) // your app package
                .scan()) {

            // 1. Handle @Value fields
            for (ClassInfo classInfo : scanResult.getAllClasses()) {
                for (FieldInfo fieldInfo : classInfo.getFieldInfo()) {
                    if (fieldInfo.hasAnnotation("org.springframework.beans.factory.annotation.Value")) {
                    	String raw = fieldInfo.getAnnotationInfo(
                    	        "org.springframework.beans.factory.annotation.Value"
                    	).getParameterValues().getValue("value").toString();


                        // Usually format: "${some.key:default}"
                        String cleaned = raw.replace("${", "")
                                            .replace("}", "")
                                            .split(":")[0]; // ignore default
                        referencedKeys.add(cleaned);
                    }
                }
            }

            // 2. Handle @ConfigurationProperties classes
            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation("org.springframework.boot.context.properties.ConfigurationProperties")) {

                String prefix = classInfo.getAnnotationInfo(
                        "org.springframework.boot.context.properties.ConfigurationProperties"
                ).getParameterValues().getValue("prefix").toString();

                // Iterate fields in that class
                classInfo.loadClass().getDeclaredFields();
                for (var field : classInfo.loadClass().getDeclaredFields()) {
                    referencedKeys.add(prefix + "." + field.getName());
                }
            }
        }

        return referencedKeys;
    }
}