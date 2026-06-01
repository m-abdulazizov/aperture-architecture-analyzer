package com.aperture.project.service;

import com.aperture.common.exception.StorageException;
import com.aperture.project.entity.Project;
import com.aperture.project.entity.ProjectStatus;
import com.aperture.project.payload.GitHubImportRequest;
import com.aperture.project.payload.GitHubImportResponse;
import com.aperture.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class GitHubImportService {

    private final ProjectRepository projectRepository;

    @Value("${aperture.storage.root:storage}")
    private String storageRoot;

    @Transactional
    public GitHubImportResponse importRepository(GitHubImportRequest request) {
        Project project = projectRepository.save(Project.builder()
                .name(request.name())
                .description(request.description())
                .originalFileName(request.repositoryUrl())
                .storedFilePath(request.repositoryUrl())
                .status(ProjectStatus.CREATED)
                .build());

        Path targetDirectory = Path.of(storageRoot).toAbsolutePath().normalize()
                .resolve("git")
                .resolve(project.getId().toString())
                .normalize();

        try {
            recreateDirectory(targetDirectory);
            Process process = new ProcessBuilder("git", "clone", "--depth", "1", request.repositoryUrl(), targetDirectory.toString())
                    .redirectErrorStream(true)
                    .start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new StorageException("Git clone failed with exit code " + exitCode);
            }

            project.setExtractedPath(targetDirectory.toString());
            project.setStatus(ProjectStatus.UPLOADED);
            projectRepository.save(project);

            return new GitHubImportResponse(
                    project.getId(),
                    request.repositoryUrl(),
                    targetDirectory.toString(),
                    project.getStatus(),
                    "Repository imported successfully"
            );
        } catch (IOException exception) {
            project.setStatus(ProjectStatus.FAILED);
            project.setFailureReason(exception.getMessage());
            projectRepository.save(project);
            throw new StorageException("Failed to import GitHub repository", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            project.setStatus(ProjectStatus.FAILED);
            project.setFailureReason(exception.getMessage());
            projectRepository.save(project);
            throw new StorageException("GitHub import was interrupted", exception);
        }
    }

    private void recreateDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            try (var paths = Files.walk(directory)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            }
        }
        Files.createDirectories(directory);
    }
}
