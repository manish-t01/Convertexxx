package com.convertexxx.service.impl;

import com.convertexxx.converter.ConverterFactory;
import com.convertexxx.converter.FileConverter;
import com.convertexxx.entity.ConversionJob;
import com.convertexxx.entity.ConversionStatus;
import com.convertexxx.repository.ConversionJobRepository;
import com.convertexxx.service.ConversionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ConversionServiceImpl implements ConversionService {

    private static final Logger log = LoggerFactory.getLogger(ConversionServiceImpl.class);

    private final ConversionJobRepository conversionJobRepository;
    private final ConverterFactory converterFactory;

    public ConversionServiceImpl(ConversionJobRepository conversionJobRepository, ConverterFactory converterFactory) {
        this.conversionJobRepository = conversionJobRepository;
        this.converterFactory = converterFactory;
    }

    @Async("conversionTaskExecutor")
    @Override
    public void processJob(UUID jobId) {
        log.info("Starting processing for job ID: {}", jobId);

        ConversionJob job = conversionJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with ID: " + jobId));

        // Update to PROCESSING state
        job.setConversionStatus(ConversionStatus.PROCESSING);
        job.setProcessingStartTime(LocalDateTime.now());
        job = conversionJobRepository.save(job);

        try {
            // Find appropriate converter
            FileConverter converter = converterFactory.getConverter(job.getOriginalFormat(), job.getTargetFormat());
            log.info("Found converter {} for job ID: {}", converter.getClass().getSimpleName(), jobId);

            // Execute conversion
            converter.convert(job);

            // On success
            job.setConversionStatus(ConversionStatus.COMPLETED);
            job.setProcessingEndTime(LocalDateTime.now());
            // outputFilePath and convertedFileName would normally be updated inside convert()
            conversionJobRepository.save(job);
            
            log.info("Successfully completed job ID: {}", jobId);

        } catch (Exception ex) {
            log.error("Failed to process job ID: {}", jobId, ex);
            job.setConversionStatus(ConversionStatus.FAILED);
            job.setErrorMessage(ex.getMessage());
            job.setProcessingEndTime(LocalDateTime.now());
            conversionJobRepository.save(job);
        }
    }
}
