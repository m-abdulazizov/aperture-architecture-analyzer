package com.aperture.scan.engine;

import com.aperture.common.exception.ScanFailedException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@Component
public class SourceFileDiscovery {

    private static final Set<String> EXCLUDED_DIRECTORIES = Set.of(
            "build",
            "target",
            ".gradle",
            ".idea",
            "out"
    );

    public List<Path> findJavaFiles(Path rootDirectory) {
        try (var paths = Files.walk(rootDirectory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !isInsideExcludedDirectory(rootDirectory, path))
                    .sorted()
                    .toList();
        } catch (IOException exception) {
            throw new ScanFailedException("Failed to discover Java source files", exception);
        }
    }

    private boolean isInsideExcludedDirectory(Path rootDirectory, Path path) {
        Path relativePath = rootDirectory.relativize(path);

        for (Path part : relativePath) {
            if (EXCLUDED_DIRECTORIES.contains(part.toString())) {
                return true;
            }
        }

        return false;
    }
}
