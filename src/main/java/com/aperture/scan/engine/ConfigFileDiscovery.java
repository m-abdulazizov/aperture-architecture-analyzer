package com.aperture.scan.engine;

import com.aperture.common.exception.ScanFailedException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class ConfigFileDiscovery {

    private static final Set<String> CONFIG_FILE_NAMES = Set.of(
            "application.yml",
            "application.yaml",
            "application.properties",
            ".env",
            "dockerfile",
            "docker-compose.yml",
            "compose.yaml"
    );

    public List<Path> findConfigFiles(Path rootDirectory) {
        try (var paths = Files.walk(rootDirectory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(this::isConfigFile)
                    .sorted()
                    .toList();
        } catch (IOException exception) {
            throw new ScanFailedException("Failed to discover configuration files", exception);
        }
    }

    private boolean isConfigFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return CONFIG_FILE_NAMES.contains(fileName);
    }
}
