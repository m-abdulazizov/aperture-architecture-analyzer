package com.aperture.scan.controller;

import com.aperture.common.response.PageResponse;
import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.payload.ScanComparisonResponse;
import com.aperture.scan.payload.ArchitectureGraphResponse;
import com.aperture.scan.payload.QualityGateResponse;
import com.aperture.scan.payload.ScanIssueFilterRequest;
import com.aperture.scan.payload.ScanIssueResponse;
import com.aperture.scan.payload.ScanJobResponse;
import com.aperture.scan.payload.ScanResultResponse;
import com.aperture.scan.service.ScanJobService;
import com.aperture.scan.service.ArchitectureGraphService;
import com.aperture.scan.service.ScanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ScanController {

    private final ScanService scanService;
    private final ScanJobService scanJobService;
    private final ArchitectureGraphService architectureGraphService;

    @PostMapping("/projects/{projectId}/scan")
    public ResponseEntity<ScanResultResponse> scanProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(scanService.scanProject(projectId));
    }

    @PostMapping("/projects/{projectId}/scan-jobs")
    public ResponseEntity<ScanJobResponse> startAsyncScan(@PathVariable UUID projectId) {
        return ResponseEntity.accepted().body(scanJobService.startAsyncScan(projectId));
    }

    @GetMapping("/projects/{projectId}/scan-jobs")
    public ResponseEntity<PageResponse<ScanJobResponse>> getProjectScanJobs(
            @PathVariable UUID projectId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponse.from(scanJobService.getProjectScanJobs(projectId, pageable)));
    }

    @GetMapping("/scan-jobs/{scanJobId}")
    public ResponseEntity<ScanJobResponse> getScanJob(@PathVariable UUID scanJobId) {
        return ResponseEntity.ok(scanJobService.getScanJob(scanJobId));
    }

    @GetMapping("/projects/{projectId}/scan-results")
    public ResponseEntity<PageResponse<ScanResultResponse>> getProjectScanResults(
            @PathVariable UUID projectId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponse.from(scanService.getProjectScanResults(projectId, pageable)));
    }

    @GetMapping("/scan-results/{scanResultId}")
    public ResponseEntity<ScanResultResponse> getScanResult(@PathVariable UUID scanResultId) {
        return ResponseEntity.ok(scanService.getScanResult(scanResultId));
    }

    @GetMapping("/scan-results/{scanResultId}/issues")
    public ResponseEntity<PageResponse<ScanIssueResponse>> getScanIssues(
            @PathVariable UUID scanResultId,
            @RequestParam(required = false) IssueSeverity severity,
            @RequestParam(required = false) IssueCategory category,
            @RequestParam(required = false) String ruleCode,
            Pageable pageable
    ) {
        ScanIssueFilterRequest filter = new ScanIssueFilterRequest(severity, category, ruleCode);
        return ResponseEntity.ok(PageResponse.from(scanService.getScanIssues(scanResultId, filter, pageable)));
    }

    @GetMapping("/scan-results/compare")
    public ResponseEntity<ScanComparisonResponse> compareScanResults(
            @RequestParam UUID from,
            @RequestParam UUID to
    ) {
        return ResponseEntity.ok(scanService.compareScanResults(from, to));
    }

    @GetMapping("/scan-results/{scanResultId}/quality-gate")
    public ResponseEntity<QualityGateResponse> getQualityGate(@PathVariable UUID scanResultId) {
        return ResponseEntity.ok(scanService.getQualityGate(scanResultId));
    }

    @DeleteMapping("/projects/{projectId}/scan-results")
    public ResponseEntity<Void> deleteOldScanResults(
            @PathVariable UUID projectId,
            @RequestParam(defaultValue = "5") int keepLast
    ) {
        scanService.deleteOldProjectScanResults(projectId, keepLast);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/projects/{projectId}/architecture/graph")
    public ResponseEntity<ArchitectureGraphResponse> getArchitectureGraph(@PathVariable UUID projectId) {
        return ResponseEntity.ok(architectureGraphService.getGraph(projectId));
    }
}
