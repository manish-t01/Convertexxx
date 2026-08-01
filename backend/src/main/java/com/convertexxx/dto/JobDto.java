package com.convertexxx.dto;

import com.convertexxx.entity.ConversionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class JobDto {
    private UUID id;
    private String originalFileName;
    private String convertedFileName;
    private String originalFormat;
    private String targetFormat;
    private Long fileSize;
    private ConversionStatus conversionStatus;
    private LocalDateTime processingStartTime;
    private LocalDateTime processingEndTime;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
