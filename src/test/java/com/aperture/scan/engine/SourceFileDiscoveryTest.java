package com.aperture.scan.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SourceFileDiscoveryTest {

    @TempDir
    Path rootDirectory;

    @Test
    void findsJavaFilesAndSkipsBuildDirectories() throws Exception {
        Path sourceFile = rootDirectory.resolve("src/main/java/com/example/UserController.java");
        Path buildFile = rootDirectory.resolve("build/generated/Ignored.java");
        Path targetFile = rootDirectory.resolve("target/generated/Ignored.java");
        Files.createDirectories(sourceFile.getParent());
        Files.createDirectories(buildFile.getParent());
        Files.createDirectories(targetFile.getParent());
        Files.writeString(sourceFile, "class UserController {}");
        Files.writeString(buildFile, "class Ignored {}");
        Files.writeString(targetFile, "class Ignored {}");

        assertThat(new SourceFileDiscovery().findJavaFiles(rootDirectory))
                .containsExactly(sourceFile);
    }
}
