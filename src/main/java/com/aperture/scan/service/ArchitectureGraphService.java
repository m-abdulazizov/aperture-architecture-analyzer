package com.aperture.scan.service;

import com.aperture.common.exception.ProjectNotFoundException;
import com.aperture.common.exception.ScanFailedException;
import com.aperture.project.entity.ProjectStatus;
import com.aperture.project.repository.ProjectRepository;
import com.aperture.scan.engine.JavaSourceParser;
import com.aperture.scan.engine.SourceFileDiscovery;
import com.aperture.scan.engine.SourceFileContext;
import com.aperture.scan.payload.ArchitectureEdgeResponse;
import com.aperture.scan.payload.ArchitectureGraphResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArchitectureGraphService {

    private final ProjectRepository projectRepository;
    private final SourceFileDiscovery sourceFileDiscovery;
    private final JavaSourceParser javaSourceParser;

    @Transactional(readOnly = true)
    public ArchitectureGraphResponse getGraph(UUID projectId) {
        var project = projectRepository.findByIdAndStatusNot(projectId, ProjectStatus.DELETED)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
        if (project.getExtractedPath() == null || !Files.isDirectory(Path.of(project.getExtractedPath()))) {
            throw new ScanFailedException("Project must be uploaded or imported before graph generation");
        }

        Path root = Path.of(project.getExtractedPath());
        List<SourceFileContext> sources = javaSourceParser.parseAll(root, sourceFileDiscovery.findJavaFiles(root));
        Set<String> knownTypes = new HashSet<>(sources.stream().map(SourceFileContext::className).toList());
        List<String> nodes = sources.stream().map(SourceFileContext::className).sorted().toList();
        List<ArchitectureEdgeResponse> edges = sources.stream()
                .flatMap(source -> source.imports().stream()
                        .map(importName -> importName.substring(importName.lastIndexOf('.') + 1))
                        .filter(knownTypes::contains)
                        .map(target -> new ArchitectureEdgeResponse(source.className(), target, dependencyType(source, target))))
                .toList();

        return new ArchitectureGraphResponse(projectId, nodes, edges);
    }

    private String dependencyType(SourceFileContext source, String target) {
        if (source.controller() && target.endsWith("Repository")) {
            return "VIOLATION";
        }
        return "USES";
    }
}
