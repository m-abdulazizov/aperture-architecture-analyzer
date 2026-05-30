package com.aperture.project.repository;

import com.aperture.project.entity.Project;
import com.aperture.project.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    Optional<Project> findByIdAndStatusNot(UUID id, ProjectStatus status);

    Page<Project> findAllByStatusNot(ProjectStatus status, Pageable pageable);

    boolean existsByNameIgnoreCaseAndStatusNot(String name, ProjectStatus status);
}
