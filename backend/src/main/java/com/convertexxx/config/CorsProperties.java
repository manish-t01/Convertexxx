package com.convertexxx.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "convertexxx.cors")
public record CorsProperties(@DefaultValue("http://localhost:3000") List<String> allowedOrigins) {
}
