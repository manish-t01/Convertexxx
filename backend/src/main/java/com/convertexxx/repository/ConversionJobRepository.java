package com.convertexxx.repository;

import com.convertexxx.entity.ConversionJob;
import com.convertexxx.entity.ConversionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversionJobRepository extends JpaRepository<ConversionJob, UUID> {
    Optional<ConversionJob> findByDownloadToken(String downloadToken);
    List<ConversionJob> findByConversionStatus(ConversionStatus status);
    void deleteByExpiresAtBefore(LocalDateTime time);
}
