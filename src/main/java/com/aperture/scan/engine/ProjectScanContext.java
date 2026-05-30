package com.aperture.scan.engine;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public record ProjectScanContext(
        UUID projectId,
        Path rootDirectory,
        List<Path> javaFiles,
        List<Path> configFiles,
        List<SourceFileContext> sourceFiles
) {

    public List<SourceFileContext> controllers() {
        return sourceFiles.stream()
                .filter(SourceFileContext::controller)
                .toList();
    }

    public List<SourceFileContext> services() {
        return sourceFiles.stream()
                .filter(SourceFileContext::service)
                .toList();
    }

    public List<SourceFileContext> repositories() {
        return sourceFiles.stream()
                .filter(SourceFileContext::repository)
                .toList();
    }

    public List<SourceFileContext> entities() {
        return sourceFiles.stream()
                .filter(SourceFileContext::entity)
                .toList();
    }
}
