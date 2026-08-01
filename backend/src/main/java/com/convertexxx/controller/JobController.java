package com.convertexxx.controller;

import com.convertexxx.dto.ApiResponse;
import com.convertexxx.dto.JobDto;
import com.convertexxx.entity.ConversionJob;
import com.convertexxx.entity.ConversionStatus;
import com.convertexxx.repository.ConversionJobRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@CrossOrigin(origins = "${cors.allowed-origins:http://localhost:3000}")
public class JobController {

    private final ConversionJobRepository conversionJobRepository;

    public JobController(ConversionJobRepository conversionJobRepository) {
        this.conversionJobRepository = conversionJobRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobDto>> getJob(@PathVariable UUID id) {
        ConversionJob job = conversionJobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with ID: " + id));

        Long convertedSize = null;
        if (job.getConversionStatus() == ConversionStatus.COMPLETED && job.getOutputFilePath() != null) {
            try {
                Path file = Paths.get(job.getOutputFilePath());
                if (Files.exists(file)) {
                    convertedSize = Files.size(file);
                }
            } catch (Exception e) {
                // Ignore exception, convertedSize remains null
            }
        }

        JobDto jobDto = JobDto.builder()
                .id(job.getId())
                .originalFileName(job.getOriginalFileName())
                .convertedFileName(job.getConvertedFileName())
                .originalFormat(job.getOriginalFormat())
                .targetFormat(job.getTargetFormat())
                .fileSize(job.getFileSize())
                .convertedFileSize(convertedSize)
                .conversionStatus(job.getConversionStatus())
                .processingStartTime(job.getProcessingStartTime())
                .processingEndTime(job.getProcessingEndTime())
                .errorMessage(job.getErrorMessage())
                .createdAt(job.getCreatedAt())
                .expiresAt(job.getExpiresAt())
                .build();

        ApiResponse<JobDto> response = ApiResponse.success("Job retrieved successfully", jobDto);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<ApiResponse<String>> getJobStatus(@PathVariable UUID id) {
        ConversionJob job = conversionJobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with ID: " + id));

        ApiResponse<String> response = ApiResponse.success("Status retrieved", job.getConversionStatus().name());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadConvertedFile(@PathVariable UUID id) {
        ConversionJob job = conversionJobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with ID: " + id));

        if (job.getConversionStatus() != ConversionStatus.COMPLETED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null); // Alternatively, throw an exception caught by GlobalExceptionHandler
        }

        if (job.getOutputFilePath() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Path file = Paths.get(job.getOutputFilePath());
        if (!Files.exists(file)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + job.getConvertedFileName() + "\"")
                .body(resource);
    }
}
