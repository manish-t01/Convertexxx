package com.convertexxx.service;

import java.util.UUID;

public interface ConversionService {
    
    /**
     * Processes a conversion job asynchronously.
     *
     * @param jobId the ID of the job to process
     */
    void processJob(UUID jobId);
}
