package com.convertexxx.dto;

import com.convertexxx.entity.ConversionStatus;
import com.convertexxx.entity.StorageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionJobResponse {
    private UUID id;
    private String originalFileName;
    private String convertedFileName;
    private String originalFormat;
    private String targetFormat;
    private Long fileSize;
    private ConversionStatus conversionStatus;
    private StorageType storageType;
    private String downloadToken;
    private Integer downloadCount;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
