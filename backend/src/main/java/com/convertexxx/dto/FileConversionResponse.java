package com.convertexxx.dto;

import com.convertexxx.entity.ConversionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class FileConversionResponse {
    private UUID id;
    private String originalFileName;
    private String convertedFileName;
    private String originalFormat;
    private String targetFormat;
    private Long fileSize;
    private ConversionStatus conversionStatus;
    private String downloadUrl;
    private LocalDateTime createdAt;
}
