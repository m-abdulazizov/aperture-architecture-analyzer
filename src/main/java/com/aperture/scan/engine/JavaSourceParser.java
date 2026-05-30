package com.aperture.scan.engine;

import com.aperture.common.exception.ScanFailedException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.ast.nodeTypes.NodeWithRange;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
public class JavaSourceParser {

    public SourceFileContext parse(Path rootDirectory, Path javaFile) {
        try {
            CompilationUnit compilationUnit = StaticJavaParser.parse(javaFile);
            TypeDeclaration<?> primaryType = findPrimaryType(compilationUnit, javaFile);
            List<String> annotations = annotationNames(primaryType);
            List<String> imports = compilationUnit.getImports().stream()
                    .map(importDeclaration -> importDeclaration.getNameAsString())
                    .toList();

            return new SourceFileContext(
                    javaFile,
                    toRelativePath(rootDirectory, javaFile),
                    compilationUnit.getPackageDeclaration()
                            .map(packageDeclaration -> packageDeclaration.getName().asString())
                            .orElse(""),
                    primaryType.getNameAsString(),
                    annotations,
                    imports,
                    fields(primaryType),
                    methods(primaryType),
                    Files.readAllLines(javaFile).size(),
                    hasAnyAnnotation(annotations, "RestController", "Controller"),
                    hasAnyAnnotation(annotations, "Service"),
                    isRepository(primaryType, annotations),
                    hasAnyAnnotation(annotations, "Entity"),
                    hasAnyAnnotation(annotations, "Configuration"),
                    compilationUnit
            );
        } catch (IOException | RuntimeException exception) {
            throw new ScanFailedException("Failed to parse Java source file: " + javaFile, exception);
        }
    }

    public List<SourceFileContext> parseAll(Path rootDirectory, List<Path> javaFiles) {
        return javaFiles.stream()
                .map(javaFile -> parse(rootDirectory, javaFile))
                .toList();
    }

    private TypeDeclaration<?> findPrimaryType(CompilationUnit compilationUnit, Path javaFile) {
        return compilationUnit.getPrimaryType()
                .orElseGet(() -> compilationUnit.getTypes().stream()
                        .findFirst()
                        .orElseThrow(() -> new ScanFailedException("No type found in Java source file: " + javaFile)));
    }

    private List<SourceFieldContext> fields(TypeDeclaration<?> type) {
        return type.getFields().stream()
                .flatMap(field -> field.getVariables().stream()
                        .map(variable -> new SourceFieldContext(
                                variable.getNameAsString(),
                                variable.getTypeAsString(),
                                annotationNames(field),
                                lineNumber(field)
                        )))
                .toList();
    }

    private List<SourceMethodContext> methods(TypeDeclaration<?> type) {
        return type.getMethods().stream()
                .map(method -> new SourceMethodContext(
                        method.getNameAsString(),
                        annotationNames(method),
                        parameters(method),
                        lineNumber(method),
                        lineCount(method)
                ))
                .toList();
    }

    private List<SourceParameterContext> parameters(MethodDeclaration method) {
        return method.getParameters().stream()
                .map(parameter -> new SourceParameterContext(
                        parameter.getNameAsString(),
                        parameter.getTypeAsString(),
                        annotationNames(parameter),
                        lineNumber(parameter)
                ))
                .toList();
    }

    private boolean isRepository(TypeDeclaration<?> type, List<String> annotations) {
        if (hasAnyAnnotation(annotations, "Repository")) {
            return true;
        }

        if (type instanceof ClassOrInterfaceDeclaration declaration) {
            return declaration.getExtendedTypes().stream()
                    .map(classOrInterfaceType -> classOrInterfaceType.getName().asString())
                    .anyMatch(typeName -> typeName.equals("JpaRepository")
                            || typeName.equals("CrudRepository")
                            || typeName.equals("PagingAndSortingRepository"));
        }

        return false;
    }

    private List<String> annotationNames(NodeWithAnnotations<?> node) {
        return node.getAnnotations().stream()
                .map(this::simpleAnnotationName)
                .toList();
    }

    private String simpleAnnotationName(AnnotationExpr annotation) {
        return annotation.getName().getIdentifier();
    }

    private boolean hasAnyAnnotation(List<String> annotations, String... expectedAnnotations) {
        for (String expectedAnnotation : expectedAnnotations) {
            if (annotations.contains(expectedAnnotation)) {
                return true;
            }
        }

        return false;
    }

    private Integer lineNumber(NodeWithRange<?> node) {
        return node.getRange()
                .map(range -> range.begin.line)
                .orElse(null);
    }

    private int lineCount(NodeWithRange<?> node) {
        return node.getRange()
                .map(range -> range.end.line - range.begin.line + 1)
                .orElse(0);
    }

    private String toRelativePath(Path rootDirectory, Path javaFile) {
        return rootDirectory.relativize(javaFile)
                .toString()
                .replace('\\', '/');
    }
}
