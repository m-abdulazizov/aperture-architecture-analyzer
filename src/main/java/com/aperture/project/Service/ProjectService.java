package com.aperture.project.Service;

import com.aperture.project.entity.Project;
import com.aperture.project.entity.ProjectStatus;
import com.aperture.project.payload.ProjectCreateRequest;
import com.aperture.project.payload.ProjectCreateResponse;
import com.aperture.project.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
