package com.aperture.scan.engine;

import com.aperture.scan.entity.IssueCategory;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.rules.DetectedIssue;
import com.aperture.scan.rules.ScannerRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ScannerEngineTest {

    @TempDir
    Path rootDirectory;

    @Test
    void buildsContextAndRunsRules() throws Exception {
        Path controller = rootDirectory.resolve("src/main/java/com/example/UserController.java");
        Path config = rootDirectory.resolve("src/main/resources/application.yaml");
        Files.createDirectories(controller.getParent());
        Files.createDirectories(config.getParent());
        Files.writeString(controller, """
                package com.example;
                import org.springframework.web.bind.annotation.RestController;
                @RestController
                public class UserController {}
                """);
        Files.writeString(config, "spring:\n  application:\n    name: sample");
        UUID projectId = UUID.randomUUID();
        ScannerEngine scannerEngine = new ScannerEngine(
                new SourceFileDiscovery(),
                new ConfigFileDiscovery(),
                new JavaSourceParser(),
                List.of(new ContextCountingRule())
        );

        List<DetectedIssue> issues = scannerEngine.scan(projectId, rootDirectory);

        assertThat(issues)
                .hasSize(1)
                .first()
                .satisfies(issue -> {
                    assertThat(issue.ruleCode()).isEqualTo("TEST_CONTEXT_COUNT");
                    assertThat(issue.filePath()).isEqualTo("src/main/java/com/example/UserController.java");
                    assertThat(issue.description()).contains(projectId.toString());
                });
    }

    private static class ContextCountingRule implements ScannerRule {

        @Override
        public String code() {
            return "TEST_CONTEXT_COUNT";
        }

        @Override
        public IssueCategory category() {
            return IssueCategory.ARCHITECTURE;
        }

        @Override
        public IssueSeverity defaultSeverity() {
            return IssueSeverity.INFO;
        }

        @Override
        public List<DetectedIssue> analyze(ProjectScanContext context) {
            SourceFileContext controller = context.controllers().getFirst();
            return List.of(new DetectedIssue(
                    category(),
                    defaultSeverity(),
                    code(),
                    "Context built",
                    "Project " + context.projectId() + " has "
                            + context.javaFiles().size() + " Java file(s) and "
                            + context.configFiles().size() + " config file(s).",
                    "No action required.",
                    controller.relativePath(),
                    1
            ));
        }
    }
}
