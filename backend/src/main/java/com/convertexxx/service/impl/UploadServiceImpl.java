package com.convertexxx.service.impl;

import com.convertexxx.dto.ConversionResponse;
import com.convertexxx.entity.ConversionJob;
import com.convertexxx.entity.ConversionStatus;
import com.convertexxx.entity.StorageType;
import com.convertexxx.exception.FileStorageException;
import com.convertexxx.exception.InvalidFileException;
import com.convertexxx.repository.ConversionJobRepository;
import com.convertexxx.service.ConversionService;
import com.convertexxx.service.UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class UploadServiceImpl implements UploadService {

    private static final Logger log = LoggerFactory.getLogger(UploadServiceImpl.class);

    private final ConversionJobRepository conversionJobRepository;
    private final ConversionService conversionService;
    private final Path uploadLocation;

    private static final List<String> SUPPORTED_FORMATS = Arrays.asList(
            "pdf", "docx", "doc", "pptx", "ppt", "xlsx", "xls",
            "png", "jpg", "jpeg", "webp", "gif", "bmp",
            "txt", "csv", "mp4", "mp3", "mov"
    );

    public UploadServiceImpl(
            ConversionJobRepository conversionJobRepository,
            ConversionService conversionService,
            @Value("${app.upload.directory:uploads/}") String uploadDir
    ) {
        this.conversionJobRepository = conversionJobRepository;
        this.conversionService = conversionService;
        this.uploadLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.uploadLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Override
    public ConversionResponse uploadFile(MultipartFile file, String targetFormat) {
        log.info("Received upload request for targetFormat: {}", targetFormat);
        
        if (file.isEmpty()) {
            throw new InvalidFileException("File is empty.");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        if (originalFileName.isEmpty()) {
            throw new InvalidFileException("Filename is missing.");
        }

        if (originalFileName.contains("..")) {
            throw new InvalidFileException("Sorry! Filename contains invalid path sequence " + originalFileName);
        }

        String originalFormat = getFileExtension(originalFileName).toLowerCase();
        if (!SUPPORTED_FORMATS.contains(originalFormat)) {
            throw new InvalidFileException("Unsupported file type: " + originalFormat);
        }

        if (targetFormat == null || targetFormat.trim().isEmpty()) {
            throw new InvalidFileException("Target format is missing.");
        }
        
        log.info("Validation passed for file: {}, size: {}", originalFileName, file.getSize());

        // Generate a secure unique filename
        String generatedFileName = UUID.randomUUID().toString() + "." + originalFormat;
        Path targetLocation = this.uploadLocation.resolve(generatedFileName);

        try {
            // Save file
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("File successfully stored at: {}", targetLocation);
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFileName + ". Please try again!", ex);
        }

        // Create database record
        ConversionJob job = ConversionJob.builder()
                .originalFileName(originalFileName)
                .convertedFileName(null)
                .originalFormat(originalFormat)
                .targetFormat(targetFormat)
                .fileSize(file.getSize())
                .conversionStatus(ConversionStatus.UPLOADED)
                .storageType(StorageType.LOCAL)
                .inputFilePath(targetLocation.toString())
                .outputFilePath(null)
                .downloadToken(UUID.randomUUID().toString())
                .downloadCount(0)
                .expiresAt(LocalDateTime.now().plusHours(24)) // E.g., expire in 24 hours
                .build();

        job = conversionJobRepository.save(job);
        log.info("Database record created successfully with job ID: {}", job.getId());

        ConversionResponse response = ConversionResponse.builder()
                .jobId(job.getId())
                .originalFileName(job.getOriginalFileName())
                .targetFormat(job.getTargetFormat())
                .status(job.getConversionStatus())
                .build();
                
        log.info("Returning success response for job ID: {}", job.getId());
        
        // Trigger async processing
        conversionService.processJob(job.getId());
        
        return response;
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        }
        return "";
    }
}
