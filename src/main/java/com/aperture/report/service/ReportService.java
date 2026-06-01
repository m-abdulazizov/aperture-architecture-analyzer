package com.aperture.report.service;

import com.aperture.common.exception.ScanFailedException;
import com.aperture.project.payload.ProjectDetailResponse;
import com.aperture.project.service.ProjectService;
import com.aperture.report.payload.IssueGroupResponse;
import com.aperture.report.payload.JsonReportResponse;
import com.aperture.report.payload.MarkdownReportResponse;
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
    private final SimplePdfService simplePdfService;

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

    @Transactional(readOnly = true)
    public MarkdownReportResponse getMarkdownReport(UUID scanResultId) {
        JsonReportResponse report = getJsonReport(scanResultId);
        StringBuilder markdown = new StringBuilder();

        markdown.append("# Aperture Scan Report\n\n");
        markdown.append("Project: ").append(report.project().name()).append("\n\n");
        markdown.append("## Summary\n\n");
        markdown.append("- Total score: ").append(report.scanResult().totalScore()).append("\n");
        markdown.append("- Total issues: ").append(report.scanResult().totalIssues()).append("\n");
        markdown.append("- Critical: ").append(report.scanResult().criticalIssues()).append("\n");
        markdown.append("- High: ").append(report.scanResult().highIssues()).append("\n");
        markdown.append("- Medium: ").append(report.scanResult().mediumIssues()).append("\n");
        markdown.append("- Low: ").append(report.scanResult().lowIssues()).append("\n\n");

        markdown.append("## Issues By Category\n\n");
        report.issuesByCategory().forEach(group ->
                markdown.append("- ").append(group.key()).append(": ").append(group.count()).append("\n"));

        markdown.append("\n## Detailed Issues\n\n");
        for (ScanIssueResponse issue : report.issues()) {
            markdown.append("### ").append(issue.title()).append("\n\n");
            markdown.append("- Severity: ").append(issue.severity()).append("\n");
            markdown.append("- Category: ").append(issue.category()).append("\n");
            markdown.append("- Rule: `").append(issue.ruleCode()).append("`\n");
            markdown.append("- File: `").append(issue.filePath()).append("`\n");
            markdown.append("- Line: ").append(issue.lineNumber() == null ? "unknown" : issue.lineNumber()).append("\n\n");
            markdown.append(issue.description()).append("\n\n");
            markdown.append("Recommendation: ").append(issue.recommendation()).append("\n\n");
        }

        return new MarkdownReportResponse(scanResultId, markdown.toString());
    }

    @Transactional(readOnly = true)
    public byte[] getPdfReport(UUID scanResultId) {
        JsonReportResponse report = getJsonReport(scanResultId);
        List<String> lines = new java.util.ArrayList<>();
        lines.add("Project: " + report.project().name());
        lines.add("Total score: " + report.scanResult().totalScore());
        lines.add("Total issues: " + report.scanResult().totalIssues());
        lines.add("Critical: " + report.scanResult().criticalIssues());
        lines.add("High: " + report.scanResult().highIssues());
        lines.add("Medium: " + report.scanResult().mediumIssues());
        lines.add("Low: " + report.scanResult().lowIssues());
        lines.add("");
        lines.add("Top issues:");
        report.issues().stream().limit(25).forEach(issue ->
                lines.add(issue.severity() + " " + issue.ruleCode() + " " + issue.filePath() + ":" + issue.lineNumber()));
        return simplePdfService.singlePageTextPdf("Aperture Scan Report", lines);
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
