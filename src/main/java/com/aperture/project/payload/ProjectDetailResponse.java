package com.aperture.project.payload;

import com.aperture.project.entity.ProjectStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectDetailResponse(
        UUID id,
        String name,
        String description,
        String originalFileName,
        String storedFilePath,
        String extractedPath,
        ProjectStatus status,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
