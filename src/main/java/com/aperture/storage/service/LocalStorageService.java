package com.aperture.storage.service;

import com.aperture.common.exception.InvalidFileException;
import com.aperture.common.exception.StorageException;
import com.aperture.storage.payload.StoredFileInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Locale;
import java.util.UUID;

@Service
public class LocalStorageService {

    private final Path storageRoot;

    public LocalStorageService(@Value("${aperture.storage.root:storage}") String storageRoot) {
        this.storageRoot = Paths.get(storageRoot).toAbsolutePath().normalize();
    }

    public StoredFileInfo storeProjectArchive(UUID projectId, MultipartFile file) {
        validateZipFile(file);

        try {
            Path uploadDirectory = getUploadDirectory(projectId);
            Files.createDirectories(uploadDirectory);

            Path targetFile = uploadDirectory.resolve("original.zip").normalize();

            if (!targetFile.startsWith(uploadDirectory)) {
                throw new InvalidFileException("Invalid file path");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return new StoredFileInfo(
                    file.getOriginalFilename(),
                    targetFile.toString(),
                    file.getSize()
            );
        } catch (IOException exception) {
            throw new StorageException("Failed to store uploaded project archive", exception);
        }
    }

    public Path getUploadDirectory(UUID projectId) {
        return resolveProjectDirectory("uploads", projectId);
    }

    public Path getExtractionDirectory(UUID projectId) {
        return resolveProjectDirectory("extracted", projectId);
    }

    private void validateZipFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Uploaded file must not be empty");
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || !originalFileName.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            throw new InvalidFileException("Only .zip project archives are allowed");
        }
    }

    private Path resolveProjectDirectory(String directoryName, UUID projectId) {
        Path directory = storageRoot
                .resolve(directoryName)
                .resolve(projectId.toString())
                .normalize();

        if (!directory.startsWith(storageRoot)) {
            throw new InvalidFileException("Invalid storage path");
        }

        return directory;
    }
}
