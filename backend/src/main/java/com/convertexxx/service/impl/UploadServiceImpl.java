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
            "pdf", "docx", "doc", "odt", "rtf", "pptx", "ppt", "odp", "xlsx", "xls", "ods",
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
    public ConversionResponse uploadFiles(List<MultipartFile> files, String targetFormat, String conversionParameters) {
        log.info("Received upload request for targetFormat: {}", targetFormat);
        
        if (files == null || files.isEmpty()) {
            throw new InvalidFileException("No files provided.");
        }

        if (targetFormat == null || targetFormat.trim().isEmpty()) {
            throw new InvalidFileException("Target format is missing.");
        }

        if (files.size() == 1) {
            return processSingleFile(files.get(0), targetFormat, conversionParameters);
        } else {
            return processMultipleFiles(files, targetFormat, conversionParameters);
        }
    }

    private ConversionResponse processSingleFile(MultipartFile file, String targetFormat, String conversionParameters) {
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

        log.info("Validation passed for file: {}, size: {}", originalFileName, file.getSize());

        String generatedFileName = UUID.randomUUID().toString() + "." + originalFormat;
        Path targetLocation = this.uploadLocation.resolve(generatedFileName);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("File successfully stored at: {}", targetLocation);
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFileName + ". Please try again!", ex);
        }

        return createAndTriggerJob(originalFileName, originalFormat, targetFormat, file.getSize(), targetLocation.toString(), conversionParameters);
    }

    private ConversionResponse processMultipleFiles(List<MultipartFile> files, String targetFormat, String conversionParameters) {
        long totalSize = 0;
        String firstFormat = "";
        
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                throw new InvalidFileException("One of the files is empty.");
            }
            
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
            if (originalFileName.contains("..")) {
                throw new InvalidFileException("Filename contains invalid path sequence " + originalFileName);
            }
            
            String format = getFileExtension(originalFileName).toLowerCase();
            if (!SUPPORTED_FORMATS.contains(format)) {
                throw new InvalidFileException("Unsupported file type: " + format);
            }
            
            if (firstFormat.isEmpty()) {
                firstFormat = format;
            }
            
            totalSize += file.getSize();
        }

        String jobDirName = UUID.randomUUID().toString();
        Path jobDir = this.uploadLocation.resolve(jobDirName);
        
        try {
            Files.createDirectories(jobDir);
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
                String format = getFileExtension(originalFileName).toLowerCase();
                String storedName = String.format("%04d_%s.%s", i, UUID.randomUUID().toString().substring(0, 8), format);
                Path targetLocation = jobDir.resolve(storedName);
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new FileStorageException("Could not store files. Please try again!", ex);
        }

        // Determine format identifier for routing
        String originalFormat = "multiple";
        if (targetFormat.equalsIgnoreCase("pdf")) {
            if (firstFormat.equalsIgnoreCase("pdf")) {
                originalFormat = "pdf"; // PDF Merge
            } else {
                originalFormat = "images"; // Image to PDF
            }
        }

        String title = files.size() + " files";
        return createAndTriggerJob(title, originalFormat, targetFormat, totalSize, jobDir.toString(), conversionParameters);
    }

    private ConversionResponse createAndTriggerJob(String originalFileName, String originalFormat, String targetFormat, long fileSize, String inputFilePath, String conversionParameters) {
        ConversionJob job = ConversionJob.builder()
                .originalFileName(originalFileName)
                .convertedFileName(null)
                .originalFormat(originalFormat)
                .targetFormat(targetFormat)
                .fileSize(fileSize)
                .conversionStatus(ConversionStatus.UPLOADED)
                .storageType(StorageType.LOCAL)
                .inputFilePath(inputFilePath)
                .outputFilePath(null)
                .downloadToken(UUID.randomUUID().toString())
                .downloadCount(0)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .conversionParameters(conversionParameters)
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
