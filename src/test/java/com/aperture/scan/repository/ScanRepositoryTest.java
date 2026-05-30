package com.aperture.scan.repository;

import com.aperture.project.entity.Project;
import com.aperture.project.entity.ProjectStatus;
import com.aperture.project.repository.ProjectRepository;
import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.entity.ScanIssue;
import com.aperture.scan.entity.ScanResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ScanRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ScanResultRepository scanResultRepository;

    @Autowired
    private ScanIssueRepository scanIssueRepository;

    @Test
    void persistsScanResultAndIssuesForProject() {
        Project project = projectRepository.save(Project.builder()
                .name("Repository Scan Test")
                .description("Temporary repository test project")
                .status(ProjectStatus.UPLOADED)
                .build());

        ScanResult scanResult = scanResultRepository.save(ScanResult.builder()
                .project(project)
                .totalScore(72)
                .architectureScore(65)
                .securityScore(85)
                .persistenceScore(70)
                .maintainabilityScore(68)
                .testingScore(72)
                .totalIssues(1)
                .criticalIssues(0)
                .highIssues(1)
                .mediumIssues(0)
                .lowIssues(0)
                .infoIssues(0)
                .startedAt(LocalDateTime.now().minusSeconds(5))
                .finishedAt(LocalDateTime.now())
                .build());

        ScanIssue scanIssue = scanIssueRepository.save(ScanIssue.builder()
                .scanResult(scanResult)
                .category(IssueCategory.ARCHITECTURE)
                .severity(IssueSeverity.HIGH)
                .ruleCode("ARCH_CONTROLLER_REPOSITORY_DEPENDENCY")
                .title("Controller directly depends on Repository")
                .description("UserController directly depends on UserRepository.")
                .recommendation("Move repository access to the service layer.")
                .filePath("src/main/java/com/example/UserController.java")
                .lineNumber(18)
                .build());

        assertThat(scanResult.getId()).isNotNull();
        assertThat(scanResult.getCreatedAt()).isNotNull();
        assertThat(scanIssue.getId()).isNotNull();
        assertThat(scanIssue.getCreatedAt()).isNotNull();

        assertThat(scanResultRepository.findAllByProjectIdOrderByCreatedAtDesc(project.getId()))
                .extracting(ScanResult::getId)
                .containsExactly(scanResult.getId());

        assertThat(scanIssueRepository.findAllByScanResultId(scanResult.getId(), PageRequest.of(0, 10)))
                .hasSize(1)
                .first()
                .extracting(ScanIssue::getRuleCode)
                .isEqualTo("ARCH_CONTROLLER_REPOSITORY_DEPENDENCY");

        assertThat(scanIssueRepository.findAllByScanResultIdAndSeverity(
                scanResult.getId(),
                IssueSeverity.HIGH,
                PageRequest.of(0, 10)
        )).hasSize(1);

        assertThat(scanIssueRepository.findAllByScanResultIdAndCategory(
                scanResult.getId(),
                IssueCategory.ARCHITECTURE,
                PageRequest.of(0, 10)
        )).hasSize(1);
    }
}
