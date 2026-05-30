package com.aperture.storage.service;

import com.aperture.common.exception.InvalidFileException;
import com.aperture.storage.payload.StoredFileInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalStorageServiceTest {

    @TempDir
    Path storageRoot;

    @Test
    void storesUploadedZipUnderProjectUploadDirectory() throws Exception {
        LocalStorageService service = new LocalStorageService(storageRoot.toString());
        UUID projectId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample.zip",
                "application/zip",
                "zip-content".getBytes()
        );

        StoredFileInfo storedFileInfo = service.storeProjectArchive(projectId, file);

        Path storedPath = Path.of(storedFileInfo.storedPath());
        assertThat(storedFileInfo.originalFileName()).isEqualTo("sample.zip");
        assertThat(storedFileInfo.size()).isEqualTo(file.getSize());
        assertThat(storedPath).exists();
        assertThat(storedPath).isEqualTo(service.getUploadDirectory(projectId).resolve("original.zip"));
        assertThat(Files.readString(storedPath)).isEqualTo("zip-content");
    }

    @Test
    void rejectsEmptyUpload() {
        LocalStorageService service = new LocalStorageService(storageRoot.toString());
        MockMultipartFile file = new MockMultipartFile("file", "sample.zip", "application/zip", new byte[0]);

        assertThatThrownBy(() -> service.storeProjectArchive(UUID.randomUUID(), file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessage("Uploaded file must not be empty");
    }

    @Test
    void rejectsNonZipUpload() {
        LocalStorageService service = new LocalStorageService(storageRoot.toString());
        MockMultipartFile file = new MockMultipartFile("file", "sample.txt", "text/plain", "content".getBytes());

        assertThatThrownBy(() -> service.storeProjectArchive(UUID.randomUUID(), file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessage("Only .zip project archives are allowed");
    }
}
