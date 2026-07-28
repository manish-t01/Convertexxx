package com.convertexxx.config;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "convertexxx.storage")
public record StorageProperties(
        @DefaultValue("./storage/uploads") Path uploadDirectory,
        @DefaultValue("./storage/temp") Path temporaryDirectory) {
}
