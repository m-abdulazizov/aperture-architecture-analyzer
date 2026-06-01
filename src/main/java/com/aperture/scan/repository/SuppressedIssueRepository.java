package com.aperture.scan.repository;

import com.aperture.scan.entity.SuppressedIssue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SuppressedIssueRepository extends JpaRepository<SuppressedIssue, UUID> {

    List<SuppressedIssue> findAllByProjectIdOrderByCreatedAtDesc(UUID projectId);

    List<SuppressedIssue> findAllByProjectId(UUID projectId);

    Optional<SuppressedIssue> findByProjectIdAndFingerprint(UUID projectId, String fingerprint);
}
