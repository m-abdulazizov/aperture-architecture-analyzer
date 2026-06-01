package com.aperture.scan.service;

import com.aperture.common.exception.ProjectNotFoundException;
import com.aperture.common.exception.ScanFailedException;
import com.aperture.project.entity.Project;
import com.aperture.project.entity.ProjectStatus;
import com.aperture.project.repository.ProjectRepository;
import com.aperture.scan.engine.ScannerEngine;
import com.aperture.scan.entity.ScanIssue;
import com.aperture.scan.entity.ScanResult;
import com.aperture.scan.payload.ScanIssueResponse;
import com.aperture.scan.payload.ScanResultResponse;
import com.aperture.scan.repository.ScanIssueRepository;
import com.aperture.scan.repository.ScanResultRepository;
import com.aperture.scan.rules.DetectedIssue;
import com.aperture.scan.scoring.ScoreBreakdown;
import com.aperture.scan.scoring.ScoreCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScanService {

    private final ProjectRepository projectRepository;
    private final ScanResultRepository scanResultRepository;
    private final ScanIssueRepository scanIssueRepository;
    private final ScannerEngine scannerEngine;
    private final ScoreCalculator scoreCalculator;

    @Transactional
    public ScanResultResponse scanProject(UUID projectId) {
        Project project = loadActiveProject(projectId);
        Path extractedPath = validateExtractedPath(project);
        LocalDateTime startedAt = LocalDateTime.now();

        try {
            project.setStatus(ProjectStatus.SCANNING);
            project.setFailureReason(null);
            projectRepository.save(project);

            List<DetectedIssue> detectedIssues = scannerEngine.scan(project.getId(), extractedPath);
            ScoreBreakdown scoreBreakdown = scoreCalculator.calculate(detectedIssues);
            ScanResult scanResult = scanResultRepository.save(toScanResult(project, scoreBreakdown, startedAt, LocalDateTime.now()));
            scanIssueRepository.saveAll(toScanIssues(scanResult, detectedIssues));

            project.setStatus(ProjectStatus.SCANNED);
            projectRepository.save(project);

            return toResponse(scanResult);
        } catch (RuntimeException exception) {
            project.setStatus(ProjectStatus.FAILED);
            project.setFailureReason(exception.getMessage());
            projectRepository.save(project);
            throw new ScanFailedException("Failed to scan project", exception);
        }
    }

    @Transactional(readOnly = true)
    public ScanResultResponse getScanResult(UUID scanResultId) {
        return scanResultRepository.findById(scanResultId)
                .map(this::toResponse)
                .orElseThrow(() -> new ScanFailedException("Scan result not found with id: " + scanResultId));
    }

    @Transactional(readOnly = true)
    public Page<ScanResultResponse> getProjectScanResults(UUID projectId, Pageable pageable) {
        return scanResultRepository.findAllByProjectId(projectId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ScanIssueResponse> getScanIssues(UUID scanResultId, Pageable pageable) {
        return scanIssueRepository.findAllByScanResultId(scanResultId, pageable)
                .map(this::toIssueResponse);
    }

    private Project loadActiveProject(UUID projectId) {
        return projectRepository.findByIdAndStatusNot(projectId, ProjectStatus.DELETED)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
    }

    private Path validateExtractedPath(Project project) {
        if (project.getExtractedPath() == null || project.getExtractedPath().isBlank()) {
            throw new ScanFailedException("Project archive must be uploaded before scanning");
        }

        Path extractedPath = Path.of(project.getExtractedPath());
        if (!Files.isDirectory(extractedPath)) {
            throw new ScanFailedException("Extracted project directory does not exist");
        }

        return extractedPath;
    }

    private ScanResult toScanResult(Project project, ScoreBreakdown scoreBreakdown, LocalDateTime startedAt, LocalDateTime finishedAt) {
        return ScanResult.builder()
                .project(project)
                .totalScore(scoreBreakdown.totalScore())
                .architectureScore(scoreBreakdown.architectureScore())
                .securityScore(scoreBreakdown.securityScore())
                .persistenceScore(scoreBreakdown.persistenceScore())
                .maintainabilityScore(scoreBreakdown.maintainabilityScore())
                .testingScore(scoreBreakdown.testingScore())
                .totalIssues(scoreBreakdown.totalIssues())
                .criticalIssues(scoreBreakdown.criticalIssues())
                .highIssues(scoreBreakdown.highIssues())
                .mediumIssues(scoreBreakdown.mediumIssues())
                .lowIssues(scoreBreakdown.lowIssues())
                .infoIssues(scoreBreakdown.infoIssues())
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .build();
    }

    private List<ScanIssue> toScanIssues(ScanResult scanResult, List<DetectedIssue> detectedIssues) {
        return detectedIssues.stream()
                .map(issue -> ScanIssue.builder()
                        .scanResult(scanResult)
                        .category(issue.category())
                        .severity(issue.severity())
                        .ruleCode(issue.ruleCode())
                        .title(issue.title())
                        .description(issue.description())
                        .recommendation(issue.recommendation())
                        .filePath(issue.filePath())
                        .lineNumber(issue.lineNumber())
                        .build())
                .toList();
    }

    private ScanResultResponse toResponse(ScanResult scanResult) {
        return new ScanResultResponse(
                scanResult.getId(),
                scanResult.getProject().getId(),
                scanResult.getTotalScore(),
                scanResult.getArchitectureScore(),
                scanResult.getSecurityScore(),
                scanResult.getPersistenceScore(),
                scanResult.getMaintainabilityScore(),
                scanResult.getTestingScore(),
                scanResult.getTotalIssues(),
                scanResult.getCriticalIssues(),
                scanResult.getHighIssues(),
                scanResult.getMediumIssues(),
                scanResult.getLowIssues(),
                scanResult.getInfoIssues(),
                scanResult.getStartedAt(),
                scanResult.getFinishedAt(),
                scanResult.getCreatedAt()
        );
    }

    private ScanIssueResponse toIssueResponse(ScanIssue scanIssue) {
        return new ScanIssueResponse(
                scanIssue.getId(),
                scanIssue.getCategory(),
                scanIssue.getSeverity(),
                scanIssue.getRuleCode(),
                scanIssue.getTitle(),
                scanIssue.getDescription(),
                scanIssue.getRecommendation(),
                scanIssue.getFilePath(),
                scanIssue.getLineNumber(),
                scanIssue.getCreatedAt()
        );
    }
}
