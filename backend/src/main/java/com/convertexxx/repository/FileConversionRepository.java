package com.convertexxx.repository;

import com.convertexxx.entity.FileConversion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileConversionRepository extends JpaRepository<FileConversion, UUID> {
    List<FileConversion> findAllByUserId(UUID userId);
}
