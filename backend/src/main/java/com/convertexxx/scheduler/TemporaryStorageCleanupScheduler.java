package com.convertexxx.scheduler;

import com.convertexxx.storage.TemporaryStorageService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TemporaryStorageCleanupScheduler {

    private final TemporaryStorageService temporaryStorageService;

    public TemporaryStorageCleanupScheduler(TemporaryStorageService temporaryStorageService) {
        this.temporaryStorageService = temporaryStorageService;
    }

    @Scheduled(cron = "${convertexxx.storage.temporary.cleanup-cron}")
    public void prepareTemporaryStorageCleanup() {
        temporaryStorageService.directoryExists();
    }
}
