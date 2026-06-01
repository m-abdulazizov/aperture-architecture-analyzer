package com.aperture.report.service;

import com.aperture.common.exception.ScanFailedException;
import com.aperture.project.payload.ProjectDetailResponse;
import com.aperture.project.service.ProjectService;
import com.aperture.report.payload.IssueGroupResponse;
import com.aperture.report.payload.JsonReportResponse;
import com.aperture.scan.payload.ScanIssueResponse;
import com.aperture.scan.payload.ScanResultResponse;
import com.aperture.scan.repository.ScanResultRepository;
import com.aperture.scan.service.ScanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ProjectService projectService;
    private final ScanService scanService;
    private final ScanResultRepository scanResultRepository;

    @Transactional(readOnly = true)
    public JsonReportResponse getJsonReport(UUID scanResultId) {
        ScanResultResponse scanResult = scanService.getScanResult(scanResultId);
        ProjectDetailResponse project = projectService.getById(scanResult.projectId());
        List<ScanIssueResponse> issues = scanService.getAllScanIssues(scanResultId);

        return new JsonReportResponse(
                project,
                scanResult,
                groupIssues(issues, issue -> issue.category().name()),
                groupIssues(issues, issue -> issue.severity().name()),
                groupIssues(issues, ScanIssueResponse::ruleCode),
                issues,
                LocalDateTime.now()
        );
    }

    @Transactional(readOnly = true)
    public JsonReportResponse getLatestProjectJsonReport(UUID projectId) {
        UUID scanResultId = scanResultRepository.findFirstByProjectIdOrderByCreatedAtDesc(projectId)
                .map(scanResult -> scanResult.getId())
                .orElseThrow(() -> new ScanFailedException("No scan result found for project: " + projectId));

        return getJsonReport(scanResultId);
    }

    private List<IssueGroupResponse> groupIssues(List<ScanIssueResponse> issues, Function<ScanIssueResponse, String> classifier) {
        return issues.stream()
                .collect(Collectors.groupingBy(classifier, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .map(entry -> new IssueGroupResponse(entry.getKey(), entry.getValue()))
                .toList();
    }
}
