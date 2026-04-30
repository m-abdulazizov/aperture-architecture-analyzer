package com.aperture.project.payload;

import com.aperture.project.entity.ProjectStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectCreateResponse(
        UUID id,
        String name,
        ProjectStatus status,
        LocalDateTime createdAt
) {
}
