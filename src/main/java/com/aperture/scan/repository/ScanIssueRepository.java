package com.aperture.scan.repository;

import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.entity.ScanIssue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ScanIssueRepository extends JpaRepository<ScanIssue, UUID> {

    Page<ScanIssue> findAllByScanResultId(UUID scanResultId, Pageable pageable);

    Page<ScanIssue> findAllByScanResultIdAndSeverity(UUID scanResultId, IssueSeverity severity, Pageable pageable);

    Page<ScanIssue> findAllByScanResultIdAndCategory(UUID scanResultId, IssueCategory category, Pageable pageable);

    Page<ScanIssue> findAllByScanResultIdAndRuleCode(UUID scanResultId, String ruleCode, Pageable pageable);

    @Query("""
            select issue
            from ScanIssue issue
            where issue.scanResult.id = :scanResultId
              and (:severity is null or issue.severity = :severity)
              and (:category is null or issue.category = :category)
              and (:ruleCode is null or issue.ruleCode = :ruleCode)
            """)
    Page<ScanIssue> findAllByFilters(
            UUID scanResultId,
            IssueSeverity severity,
            IssueCategory category,
            String ruleCode,
            Pageable pageable
    );

    List<ScanIssue> findAllByScanResultId(UUID scanResultId);

    void deleteAllByScanResultId(UUID scanResultId);
}
