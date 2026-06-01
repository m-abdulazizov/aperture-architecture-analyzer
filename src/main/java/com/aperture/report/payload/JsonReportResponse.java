package com.aperture.report.payload;

import com.aperture.project.payload.ProjectDetailResponse;
import com.aperture.scan.payload.ScanIssueResponse;
import com.aperture.scan.payload.ScanResultResponse;

import java.time.LocalDateTime;
import java.util.List;

public record JsonReportResponse(
        ProjectDetailResponse project,
        ScanResultResponse scanResult,
        List<IssueGroupResponse> issuesByCategory,
        List<IssueGroupResponse> issuesBySeverity,
        List<IssueGroupResponse> issuesByRule,
        List<ScanIssueResponse> issues,
        LocalDateTime generatedAt
) {
}
