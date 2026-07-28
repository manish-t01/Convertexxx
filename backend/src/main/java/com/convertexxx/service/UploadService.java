package com.convertexxx.service;

import com.convertexxx.dto.ConversionResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    ConversionResponse uploadFile(MultipartFile file, String targetFormat);
}
