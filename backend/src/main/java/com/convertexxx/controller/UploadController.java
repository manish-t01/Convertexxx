package com.convertexxx.controller;

import com.convertexxx.dto.ApiResponse;
import com.convertexxx.dto.ConversionResponse;
import com.convertexxx.service.UploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<ApiResponse<ConversionResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("targetFormat") String targetFormat) {
            
        ConversionResponse response = uploadService.uploadFile(file, targetFormat);
        
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", response));
    }
}
