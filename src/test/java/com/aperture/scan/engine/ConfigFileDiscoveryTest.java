package com.aperture.scan.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigFileDiscoveryTest {

    @TempDir
    Path rootDirectory;

    @Test
    void findsSupportedConfigFiles() throws Exception {
        Path yaml = rootDirectory.resolve("src/main/resources/application.yaml");
        Path env = rootDirectory.resolve(".env");
        Path dockerfile = rootDirectory.resolve("Dockerfile");
        Path ignored = rootDirectory.resolve("README.md");
        Files.createDirectories(yaml.getParent());
        Files.writeString(yaml, "spring:\n  application:\n    name: sample");
        Files.writeString(env, "TOKEN=value");
        Files.writeString(dockerfile, "FROM eclipse-temurin:21");
        Files.writeString(ignored, "# Sample");

        assertThat(new ConfigFileDiscovery().findConfigFiles(rootDirectory))
                .containsExactly(env, dockerfile, yaml);
    }
}
