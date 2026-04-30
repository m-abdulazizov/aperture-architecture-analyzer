package com.aperture.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "projects")
public class Project
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "original_file_name", length = 500)
    private String originalFileName;

    @Column(name = "stored_file_path", length = 1000)
    private String storedFilePath;

    @Column(name = "extracted_path", length = 1000)
    private String extractedPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProjectStatus status;

    @Column(name = "failure_reason", columnDefinition = "text")
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist(){
        this.createdAt = LocalDateTime.now();

        if(this.status == null)
        {
            this.status = ProjectStatus.CREATED;
        }
    }

    @PreUpdate
    void preUpdate(){
        this.updatedAt = LocalDateTime.now();
    }
}
