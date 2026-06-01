package com.aperture.scan.repository;

import com.aperture.scan.entity.ScanResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScanResultRepository extends JpaRepository<ScanResult, UUID> {

    List<ScanResult> findAllByProjectIdOrderByCreatedAtDesc(UUID projectId);

    Page<ScanResult> findAllByProjectId(UUID projectId, Pageable pageable);

    Optional<ScanResult> findFirstByProjectIdOrderByCreatedAtDesc(UUID projectId);

    long countByProjectId(UUID projectId);
}
