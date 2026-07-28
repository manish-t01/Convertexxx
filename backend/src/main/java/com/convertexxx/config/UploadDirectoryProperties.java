package com.convertexxx.config;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "convertexxx.storage.upload")
public record UploadDirectoryProperties(@DefaultValue("./storage/uploads") Path directory) {
}
