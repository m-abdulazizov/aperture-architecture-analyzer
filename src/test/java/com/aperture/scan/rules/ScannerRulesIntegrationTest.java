package com.aperture.scan.rules;

import com.aperture.scan.engine.ScannerEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ScannerRulesIntegrationTest {

    @Autowired
    private ScannerEngine scannerEngine;

    @TempDir
    Path rootDirectory;

    @Test
    void detectsMvpRuleViolationsAcrossSampleProject() throws Exception {
        writeSource("src/main/java/com/example/User.java", """
                package com.example;

                import jakarta.persistence.CascadeType;
                import jakarta.persistence.Entity;
                import jakarta.persistence.FetchType;
                import jakarta.persistence.ManyToOne;

                @Entity
                public class User {
                    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
                    private Department department;
                }
                """);
        writeSource("src/main/java/com/example/UserRepository.java", """
                package com.example;

                import org.springframework.data.jpa.repository.JpaRepository;

                public interface UserRepository extends JpaRepository<User, Long> {}
                """);
        writeSource("src/main/java/com/example/UserController.java", """
                package com.example;

                import java.util.List;
                import org.springframework.beans.factory.annotation.Autowired;
                import org.springframework.transaction.annotation.Transactional;
                import org.springframework.web.bind.annotation.GetMapping;
                import org.springframework.web.bind.annotation.PostMapping;
                import org.springframework.web.bind.annotation.RequestBody;
                import org.springframework.web.bind.annotation.RestController;

                @RestController
                public class UserController {
                    @Autowired
                    private UserRepository userRepository;

                    @Transactional
                    @PostMapping("/users")
                    public User create(@RequestBody CreateUserRequest request) {
                        return new User();
                    }

                    @GetMapping("/users")
                    public List<User> list() {
                        return List.of();
                    }
                }
                """);
        writeFile("src/main/resources/application.yaml", """
                spring:
                  datasource:
                    password: root
                """);

        List<DetectedIssue> issues = scannerEngine.scan(UUID.randomUUID(), rootDirectory);

        assertThat(issues)
                .extracting(DetectedIssue::ruleCode)
                .contains(
                        "ARCH_CONTROLLER_REPOSITORY_DEPENDENCY",
                        "ARCH_REPOSITORY_USED_OUTSIDE_SERVICE",
                        "ARCH_ENTITY_RETURNED_FROM_CONTROLLER",
                        "SPRING_FIELD_INJECTION",
                        "SPRING_TRANSACTIONAL_ON_CONTROLLER",
                        "SPRING_REQUEST_BODY_WITHOUT_VALID",
                        "SPRING_GET_LIST_WITHOUT_PAGINATION",
                        "JPA_EAGER_RELATIONSHIP",
                        "JPA_CASCADE_ALL_USAGE",
                        "SECURITY_HARDCODED_SECRET"
                );
    }

    private void writeSource(String relativePath, String content) throws Exception {
        writeFile(relativePath, content);
    }

    private void writeFile(String relativePath, String content) throws Exception {
        Path file = rootDirectory.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }
}
