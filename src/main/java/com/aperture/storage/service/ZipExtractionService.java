package com.aperture.storage.service;

import com.aperture.common.exception.InvalidFileException;
import com.aperture.common.exception.StorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class ZipExtractionService {

    private final LocalStorageService localStorageService;

    public Path extract(UUID projectId, Path zipPath) {
        Path extractionDirectory = localStorageService.getExtractionDirectory(projectId);
        validateZipPath(zipPath);

        try {
            recreateDirectory(extractionDirectory);

            try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipPath))) {
                ZipEntry entry;

                while ((entry = zipInputStream.getNextEntry()) != null) {
                    String entryName = normalizeEntryName(entry);
                    Path targetPath = extractionDirectory.resolve(entryName).normalize();

                    if (!targetPath.startsWith(extractionDirectory)) {
                        throw new InvalidFileException("Invalid ZIP file: unsafe path detected");
                    }

                    if (isDirectoryEntry(entry)) {
                        Files.createDirectories(targetPath);
                    } else {
                        Path parent = targetPath.getParent();

                        if (parent != null) {
                            Files.createDirectories(parent);
                        }

                        Files.copy(zipInputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }

                    zipInputStream.closeEntry();
                }
            }

            return extractionDirectory;
        } catch (IOException exception) {
            throw new StorageException("Failed to extract project archive", exception);
        }
    }

    private void recreateDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            try (var paths = Files.walk(directory)) {
                paths.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException exception) {
                                throw new StorageException("Failed to clean extraction directory", exception);
                            }
                        });
            }
        }

        Files.createDirectories(directory);
    }

    private void validateZipPath(Path zipPath) {
        if (zipPath == null || !Files.isRegularFile(zipPath)) {
            throw new InvalidFileException("ZIP archive does not exist");
        }
    }

    private String normalizeEntryName(ZipEntry entry) {
        return entry.getName().replace('\\', '/');
    }

    private boolean isDirectoryEntry(ZipEntry entry) {
        String entryName = entry.getName();
        return entry.isDirectory() || entryName.endsWith("/") || entryName.endsWith("\\");
    }
}
