package com.aperture.project.service;

import com.aperture.project.entity.Project;
import com.aperture.project.entity.ProjectStatus;
import com.aperture.project.payload.ProjectCreateRequest;
import com.aperture.project.payload.ProjectCreateResponse;
import com.aperture.project.payload.ProjectStatsResponse;
import com.aperture.project.repository.ProjectRepository;
import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.ScanResult;
import com.aperture.scan.repository.ScanIssueRepository;
import com.aperture.scan.repository.ScanResultRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.aperture.common.exception.ProjectNotFoundException;
import com.aperture.project.payload.ProjectDetailResponse;
import com.aperture.project.payload.ProjectUploadResponse;
import com.aperture.storage.payload.StoredFileInfo;
import com.aperture.storage.service.LocalStorageService;
import com.aperture.storage.service.ZipExtractionService;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final LocalStorageService localStorageService;
    private final ZipExtractionService zipExtractionService;
    private final ScanResultRepository scanResultRepository;
    private final ScanIssueRepository scanIssueRepository;

    @Transactional
    public ProjectCreateResponse create(ProjectCreateRequest request) {
        if (projectRepository.existsByNameIgnoreCaseAndStatusNot(request.name(), ProjectStatus.DELETED)) {
            throw new IllegalArgumentException("Project with this name already exists");
        }

        Project project = Project.builder()
                .name(request.name())
                .description(request.description())
                .status(ProjectStatus.CREATED)
                .build();

        Project savedProject = projectRepository.save(project);

        return new ProjectCreateResponse(
                savedProject.getId(),
                savedProject.getName(),
                savedProject.getStatus(),
                savedProject.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public ProjectDetailResponse getById(UUID id) {
        Project project = projectRepository.findByIdAndStatusNot(id, ProjectStatus.DELETED)
                .orElseThrow(()->new ProjectNotFoundException("Project not found with id: " + id));

        return toDetailResponse(project);
    }

    private ProjectDetailResponse toDetailResponse(Project project) {
        return new ProjectDetailResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOriginalFileName(),
                project.getStoredFilePath(),
                project.getExtractedPath(),
                project.getStatus(),
                project.getFailureReason(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public Page<ProjectDetailResponse> getAll(Pageable pageable) {
        return projectRepository.findAllByStatusNot(ProjectStatus.DELETED, pageable)
                .map(this::toDetailResponse);
    }

    @Transactional
    public void delete(UUID id) {
        Project project = projectRepository.findByIdAndStatusNot(id, ProjectStatus.DELETED)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + id));

        project.setStatus(ProjectStatus.DELETED);
        projectRepository.save(project);
    }

    @Transactional
    public ProjectUploadResponse upload(UUID id, MultipartFile file) {
        Project project = projectRepository.findByIdAndStatusNot(id, ProjectStatus.DELETED)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + id));

        StoredFileInfo storedFileInfo = localStorageService.storeProjectArchive(project.getId(), file);

        Path extractedPath = zipExtractionService.extract(
                project.getId(),
                Path.of(storedFileInfo.storedPath())
        );

        project.setOriginalFileName(storedFileInfo.originalFileName());
        project.setStoredFilePath(storedFileInfo.storedPath());
        project.setExtractedPath(extractedPath.toString());
        project.setStatus(ProjectStatus.UPLOADED);
        project.setFailureReason(null);

        projectRepository.save(project);

        return new ProjectUploadResponse(
                project.getId(),
                project.getOriginalFileName(),
                project.getStatus(),
                "Project archive uploaded and extracted successfully"
        );
    }

    @Transactional(readOnly = true)
    public ProjectStatsResponse getStats(UUID id) {
        Project project = projectRepository.findByIdAndStatusNot(id, ProjectStatus.DELETED)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + id));
        var scanResults = scanResultRepository.findAllByProjectIdOrderByCreatedAtDesc(project.getId());
        ScanResult latest = scanResults.isEmpty() ? null : scanResults.getFirst();
        ScanResult previous = scanResults.size() < 2 ? null : scanResults.get(1);
        Map<IssueCategory, Long> issuesByCategory = latest == null
                ? Map.of()
                : scanIssueRepository.findAllByScanResultId(latest.getId()).stream()
                        .collect(Collectors.groupingBy(issue -> issue.getCategory(), Collectors.counting()));

        return new ProjectStatsResponse(
                project.getId(),
                scanResults.size(),
                latest == null ? null : latest.getTotalScore(),
                previous == null ? null : previous.getTotalScore(),
                latest == null || previous == null ? null : latest.getTotalScore() - previous.getTotalScore(),
                latest == null ? null : latest.getCreatedAt(),
                issuesByCategory
        );
    }
}
