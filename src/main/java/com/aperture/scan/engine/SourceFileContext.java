package com.aperture.scan.engine;

import com.github.javaparser.ast.CompilationUnit;

import java.nio.file.Path;
import java.util.List;

public record SourceFileContext(
        Path filePath,
        String relativePath,
        String packageName,
        String className,
        List<String> annotations,
        List<String> imports,
        List<SourceFieldContext> fields,
        List<SourceMethodContext> methods,
        int lineCount,
        boolean controller,
        boolean service,
        boolean repository,
        boolean entity,
        boolean configuration,
        CompilationUnit compilationUnit
) {
}
