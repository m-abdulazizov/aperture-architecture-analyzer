package com.aperture.project.payload;

import com.aperture.project.entity.ProjectStatus;

import java.util.UUID;

public record ProjectUploadResponse(
        UUID projectId,
        String originalFileName,
        ProjectStatus status,
        String message
) {}
