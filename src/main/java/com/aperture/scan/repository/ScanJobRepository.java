package com.aperture.scan.repository;

import com.aperture.scan.entity.ScanJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ScanJobRepository extends JpaRepository<ScanJob, UUID> {

    Page<ScanJob> findAllByProjectId(UUID projectId, Pageable pageable);
}
