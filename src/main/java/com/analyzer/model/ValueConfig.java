package com.analyzer.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ValueConfig {

    @Value("${app.url}")
    private String url;

    @Value("${app.timeout}")
    private int timeout;

    public String getUrl() {
        return url;
    }

    public int getTimeout() {
        return timeout;
    }
}