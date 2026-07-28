package com.convertexxx.dto;

import com.convertexxx.entity.ConversionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionResponse {
    private UUID jobId;
    private ConversionStatus status;
    private String downloadUrl;
    private String message;
}
