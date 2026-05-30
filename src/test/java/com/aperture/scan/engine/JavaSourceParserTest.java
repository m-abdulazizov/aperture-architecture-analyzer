package com.aperture.scan.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JavaSourceParserTest {

    @TempDir
    Path rootDirectory;

    @Test
    void parsesControllerSourceMetadata() throws Exception {
        Path sourceFile = writeSource(
                "src/main/java/com/example/user/UserController.java",
                """
                        package com.example.user;

                        import jakarta.validation.Valid;
                        import org.springframework.web.bind.annotation.PostMapping;
                        import org.springframework.web.bind.annotation.RequestBody;
                        import org.springframework.web.bind.annotation.RestController;

                        @RestController
                        public class UserController {
                            private final UserService userService;

                            @PostMapping("/users")
                            public void create(@Valid @RequestBody CreateUserRequest request) {
                                userService.create(request);
                            }
                        }
                        """
        );

        SourceFileContext context = new JavaSourceParser().parse(rootDirectory, sourceFile);

        assertThat(context.relativePath()).isEqualTo("src/main/java/com/example/user/UserController.java");
        assertThat(context.packageName()).isEqualTo("com.example.user");
        assertThat(context.className()).isEqualTo("UserController");
        assertThat(context.controller()).isTrue();
        assertThat(context.service()).isFalse();
        assertThat(context.imports()).contains("org.springframework.web.bind.annotation.RestController");
        assertThat(context.fields())
                .extracting(SourceFieldContext::type)
                .containsExactly("UserService");
        assertThat(context.methods())
                .extracting(SourceMethodContext::name)
                .containsExactly("create");
        assertThat(context.methods().getFirst().parameters().getFirst().annotations())
                .containsExactly("Valid", "RequestBody");
        assertThat(context.lineCount()).isGreaterThan(10);
        assertThat(context.compilationUnit()).isNotNull();
    }

    @Test
    void classifiesServiceRepositoryEntityAndConfigurationTypes() throws Exception {
        List<Path> files = List.of(
                writeSource("src/main/java/com/example/UserService.java", """
                        package com.example;
                        import org.springframework.stereotype.Service;
                        @Service
                        public class UserService {}
                        """),
                writeSource("src/main/java/com/example/UserRepository.java", """
                        package com.example;
                        import org.springframework.data.jpa.repository.JpaRepository;
                        public interface UserRepository extends JpaRepository<User, Long> {}
                        """),
                writeSource("src/main/java/com/example/User.java", """
                        package com.example;
                        import jakarta.persistence.Entity;
                        @Entity
                        public class User {}
                        """),
                writeSource("src/main/java/com/example/AppConfig.java", """
                        package com.example;
                        import org.springframework.context.annotation.Configuration;
                        @Configuration
                        public class AppConfig {}
                        """)
        );

        List<SourceFileContext> contexts = new JavaSourceParser().parseAll(rootDirectory, files);

        assertThat(contexts).filteredOn(SourceFileContext::service).hasSize(1);
        assertThat(contexts).filteredOn(SourceFileContext::repository).hasSize(1);
        assertThat(contexts).filteredOn(SourceFileContext::entity).hasSize(1);
        assertThat(contexts).filteredOn(SourceFileContext::configuration).hasSize(1);
    }

    private Path writeSource(String relativePath, String content) throws Exception {
        Path sourceFile = rootDirectory.resolve(relativePath);
        Files.createDirectories(sourceFile.getParent());
        Files.writeString(sourceFile, content);
        return sourceFile;
    }
}
