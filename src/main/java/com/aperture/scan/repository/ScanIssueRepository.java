package com.aperture.scan.repository;

import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.entity.ScanIssue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ScanIssueRepository extends JpaRepository<ScanIssue, UUID> {

    Page<ScanIssue> findAllByScanResultId(UUID scanResultId, Pageable pageable);

    Page<ScanIssue> findAllByScanResultIdAndSeverity(UUID scanResultId, IssueSeverity severity, Pageable pageable);

    Page<ScanIssue> findAllByScanResultIdAndCategory(UUID scanResultId, IssueCategory category, Pageable pageable);

    Page<ScanIssue> findAllByScanResultIdAndRuleCode(UUID scanResultId, String ruleCode, Pageable pageable);
}
