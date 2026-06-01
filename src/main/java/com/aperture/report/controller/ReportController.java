package com.aperture.report.controller;

import com.aperture.report.payload.JsonReportResponse;
import com.aperture.report.payload.MarkdownReportResponse;
import com.aperture.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/scan-results/{scanResultId}/report/json")
    public ResponseEntity<JsonReportResponse> getJsonReport(@PathVariable UUID scanResultId) {
        return ResponseEntity.ok(reportService.getJsonReport(scanResultId));
    }

    @GetMapping("/projects/{projectId}/report/json")
    public ResponseEntity<JsonReportResponse> getLatestProjectJsonReport(@PathVariable UUID projectId) {
        return ResponseEntity.ok(reportService.getLatestProjectJsonReport(projectId));
    }

    @GetMapping("/scan-results/{scanResultId}/report/markdown")
    public ResponseEntity<MarkdownReportResponse> getMarkdownReport(@PathVariable UUID scanResultId) {
        return ResponseEntity.ok(reportService.getMarkdownReport(scanResultId));
    }

    @GetMapping("/scan-results/{scanResultId}/report/pdf")
    public ResponseEntity<byte[]> getPdfReport(@PathVariable UUID scanResultId) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=aperture-report-" + scanResultId + ".pdf")
                .body(reportService.getPdfReport(scanResultId));
    }
}
