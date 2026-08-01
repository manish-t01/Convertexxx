package com.convertexxx.controller;

import com.convertexxx.dto.ApiResponse;
import com.convertexxx.dto.ConversionResponse;
import com.convertexxx.service.UploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    /**
     * Uploads a file for conversion.
     * 
     * @param file the MultipartFile received from the client
     * @param targetFormat the desired format to convert the file into
     * @return ApiResponse containing the job ID and upload status
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<ConversionResponse>> uploadFiles(
            @RequestParam("file") List<MultipartFile> files,
            @RequestParam("targetFormat") String targetFormat,
            @RequestParam(value = "conversionParameters", required = false) String conversionParameters) {
            
        ConversionResponse response = uploadService.uploadFiles(files, targetFormat, conversionParameters);
        
        return ResponseEntity.ok(ApiResponse.success("Files uploaded successfully", response));
    }
}
