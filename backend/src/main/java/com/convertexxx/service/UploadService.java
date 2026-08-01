package com.convertexxx.service;

import com.convertexxx.dto.ConversionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UploadService {
    ConversionResponse uploadFiles(List<MultipartFile> files, String targetFormat, String conversionParameters);
}
