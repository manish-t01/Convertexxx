package com.convertexxx.config;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "convertexxx.storage.temporary")
public record TemporaryStorageProperties(
        @DefaultValue("./storage/temp") Path directory,
        @DefaultValue("0 0 * * * *") String cleanupCron) {
}
