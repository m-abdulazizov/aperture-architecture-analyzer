package com.aperture.project.payload;

import com.aperture.project.entity.ProjectStatus;

import java.util.UUID;

public record GitHubImportResponse(
        UUID projectId,
        String repositoryUrl,
        String extractedPath,
        ProjectStatus status,
        String message
) {
}
