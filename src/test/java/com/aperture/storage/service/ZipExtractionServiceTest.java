package com.aperture.storage.service;

import com.aperture.common.exception.InvalidFileException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZipExtractionServiceTest {

    @TempDir
    Path storageRoot;

    @Test
    void extractsAllFilesFromArchive() throws Exception {
        LocalStorageService localStorageService = new LocalStorageService(storageRoot.toString());
        ZipExtractionService service = new ZipExtractionService(localStorageService);
        UUID projectId = UUID.randomUUID();
        Path zipPath = storageRoot.resolve("project.zip");
        createZip(zipPath, Map.of(
                "src/main/java/App.java", "class App {}",
                "src/main/resources/application.yaml", "spring:\n  application:\n    name: sample"
        ));

        Path extractionDirectory = service.extract(projectId, zipPath);

        assertThat(extractionDirectory).isEqualTo(localStorageService.getExtractionDirectory(projectId));
        assertThat(extractionDirectory.resolve("src/main/java/App.java")).hasContent("class App {}");
        assertThat(extractionDirectory.resolve("src/main/resources/application.yaml"))
                .hasContent("spring:\n  application:\n    name: sample");
    }

    @Test
    void extractsArchivesWithWindowsDirectorySeparators() throws Exception {
        LocalStorageService localStorageService = new LocalStorageService(storageRoot.toString());
        ZipExtractionService service = new ZipExtractionService(localStorageService);
        UUID projectId = UUID.randomUUID();
        Path zipPath = storageRoot.resolve("windows-style.zip");

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            zipOutputStream.putNextEntry(new ZipEntry("src\\main\\"));
            zipOutputStream.closeEntry();
            zipOutputStream.putNextEntry(new ZipEntry("src\\main\\java\\"));
            zipOutputStream.closeEntry();
            zipOutputStream.putNextEntry(new ZipEntry("src\\main\\java\\Demo.java"));
            zipOutputStream.write("class Demo {}".getBytes());
            zipOutputStream.closeEntry();
        }

        Path extractionDirectory = service.extract(projectId, zipPath);

        assertThat(extractionDirectory.resolve("src/main/java/Demo.java")).hasContent("class Demo {}");
    }

    @Test
    void rejectsArchiveEntriesOutsideExtractionDirectory() throws Exception {
        ZipExtractionService service = new ZipExtractionService(new LocalStorageService(storageRoot.toString()));
        Path zipPath = storageRoot.resolve("unsafe.zip");
        createZip(zipPath, Map.of("../outside.txt", "unsafe"));

        assertThatThrownBy(() -> service.extract(UUID.randomUUID(), zipPath))
                .isInstanceOf(InvalidFileException.class)
                .hasMessage("Invalid ZIP file: unsafe path detected");
    }

    @Test
    void rejectsMissingArchive() {
        ZipExtractionService service = new ZipExtractionService(new LocalStorageService(storageRoot.toString()));

        assertThatThrownBy(() -> service.extract(UUID.randomUUID(), storageRoot.resolve("missing.zip")))
                .isInstanceOf(InvalidFileException.class)
                .hasMessage("ZIP archive does not exist");
    }

    private void createZip(Path zipPath, Map<String, String> entries) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                zipOutputStream.putNextEntry(new ZipEntry(entry.getKey()));
                zipOutputStream.write(entry.getValue().getBytes());
                zipOutputStream.closeEntry();
            }
        }
    }
}
