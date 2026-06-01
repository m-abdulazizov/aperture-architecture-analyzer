package com.aperture.scan.controller;

import com.aperture.common.response.PageResponse;
import com.aperture.scan.payload.ScanIssueResponse;
import com.aperture.scan.payload.ScanResultResponse;
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

    @PostMapping("/projects/{projectId}/scan")
    public ResponseEntity<ScanResultResponse> scanProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(scanService.scanProject(projectId));
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
            Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponse.from(scanService.getScanIssues(scanResultId, pageable)));
    }
}
