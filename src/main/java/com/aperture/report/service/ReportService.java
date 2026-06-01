package com.aperture.report.service;

import com.aperture.common.exception.ScanFailedException;
import com.aperture.project.payload.ProjectDetailResponse;
import com.aperture.project.service.ProjectService;
import com.aperture.report.payload.JsonReportResponse;
import com.aperture.scan.payload.ScanIssueResponse;
import com.aperture.scan.payload.ScanResultResponse;
import com.aperture.scan.repository.ScanIssueRepository;
import com.aperture.scan.repository.ScanResultRepository;
import com.aperture.scan.service.ScanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ProjectService projectService;
    private final ScanService scanService;
    private final ScanResultRepository scanResultRepository;
    private final ScanIssueRepository scanIssueRepository;

    @Transactional(readOnly = true)
    public JsonReportResponse getJsonReport(UUID scanResultId) {
        ScanResultResponse scanResult = scanService.getScanResult(scanResultId);
        ProjectDetailResponse project = projectService.getById(scanResult.projectId());
        List<ScanIssueResponse> issues = scanService.getScanIssues(scanResultId, PageRequest.of(0, 1000)).getContent();

        return new JsonReportResponse(project, scanResult, issues, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public JsonReportResponse getLatestProjectJsonReport(UUID projectId) {
        UUID scanResultId = scanResultRepository.findFirstByProjectIdOrderByCreatedAtDesc(projectId)
                .map(scanResult -> scanResult.getId())
                .orElseThrow(() -> new ScanFailedException("No scan result found for project: " + projectId));

        return getJsonReport(scanResultId);
    }
}
