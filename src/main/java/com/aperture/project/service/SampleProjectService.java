package com.aperture.project.service;

import com.aperture.common.exception.StorageException;
import com.aperture.project.payload.SampleZipResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class SampleProjectService {

    private static final Path SAMPLE_ROOT = Path.of("samples", "vulnerable-spring", "src");
    private static final Path SAMPLE_ZIP = Path.of("build", "tmp", "vulnerable-spring.zip");

    public SampleZipResponse packageVulnerableSample() {
        try {
            Files.createDirectories(SAMPLE_ZIP.getParent());
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(SAMPLE_ZIP))) {
                try (var paths = Files.walk(SAMPLE_ROOT)) {
                    for (Path path : paths.filter(Files::isRegularFile).toList()) {
                        String entryName = SAMPLE_ROOT.relativize(path).toString().replace('\\', '/');
                        zipOutputStream.putNextEntry(new ZipEntry(entryName));
                        Files.copy(path, zipOutputStream);
                        zipOutputStream.closeEntry();
                    }
                }
            }

            return new SampleZipResponse(
                    SAMPLE_ZIP.toAbsolutePath().normalize().toString(),
                    "Sample ZIP generated successfully"
            );
        } catch (IOException exception) {
            throw new StorageException("Failed to package vulnerable sample project", exception);
        }
    }
}
