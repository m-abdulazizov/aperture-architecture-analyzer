package com.aperture.project.service;

import com.aperture.project.entity.Project;
import com.aperture.project.entity.ProjectStatus;
import com.aperture.project.payload.ProjectCreateRequest;
import com.aperture.project.payload.ProjectCreateResponse;
import com.aperture.project.repository.ProjectRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.aperture.common.exception.ProjectNotFoundException;
import com.aperture.project.payload.ProjectDetailResponse;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService
{
    private final ProjectRepository projectRepository;

    @Transactional
    public ProjectCreateResponse create(ProjectCreateRequest request)
    {
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
    public ProjectDetailResponse getById(UUID id){
        Project project = projectRepository.findById(id)
                .orElseThrow(()->new ProjectNotFoundException("Project not found with id: " + id));

        return toDetailResponse(project);
    }

    private ProjectDetailResponse toDetailResponse(Project project)
    {
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
}
