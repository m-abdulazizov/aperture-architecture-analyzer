package com.aperture.scan.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ScanRestFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUploadScanFilterAndReportFlowWorks() throws Exception {
        String projectBody = """
                {
                  "name": "REST Flow %s",
                  "description": "Integration test project"
                }
                """.formatted(UUID.randomUUID());

        JsonNode project = objectMapper.readTree(mockMvc.perform(post("/api/v1/projects")
                        .contentType("application/json")
                        .content(projectBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString());

        String projectId = project.get("id").asText();
        MockMultipartFile archive = new MockMultipartFile(
                "file",
                "sample.zip",
                "application/zip",
                sampleArchive()
        );

        mockMvc.perform(multipart("/api/v1/projects/{projectId}/upload", projectId)
                        .file(archive))
                .andExpect(status().isOk());

        JsonNode scan = objectMapper.readTree(mockMvc.perform(post("/api/v1/projects/{projectId}/scan", projectId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());

        String scanResultId = scan.get("id").asText();
        assertThat(scan.get("totalIssues").asInt()).isGreaterThan(0);

        JsonNode filteredIssues = objectMapper.readTree(mockMvc.perform(get("/api/v1/scan-results/{scanResultId}/issues", scanResultId)
                        .param("severity", "CRITICAL")
                        .param("category", "SECURITY"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());

        assertThat(filteredIssues.get("content")).hasSize(1);

        JsonNode report = objectMapper.readTree(mockMvc.perform(get("/api/v1/scan-results/{scanResultId}/report/json", scanResultId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());

        assertThat(report.get("issues")).hasSize(scan.get("totalIssues").asInt());
        assertThat(report.get("issuesByCategory")).isNotEmpty();

        JsonNode qualityGate = objectMapper.readTree(mockMvc.perform(get("/api/v1/scan-results/{scanResultId}/quality-gate", scanResultId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());

        assertThat(qualityGate.get("passed").asBoolean()).isFalse();
    }

    private byte[] sampleArchive() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            writeEntry(zipOutputStream, "src/main/java/com/example/User.java", """
                    package com.example;
                    import jakarta.persistence.*;
                    @Entity
                    public class User {
                        @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
                        private Department department;
                    }
                    """);
            writeEntry(zipOutputStream, "src/main/java/com/example/UserRepository.java", """
                    package com.example;
                    import org.springframework.data.jpa.repository.JpaRepository;
                    public interface UserRepository extends JpaRepository<User, Long> {}
                    """);
            writeEntry(zipOutputStream, "src/main/java/com/example/UserController.java", """
                    package com.example;
                    import org.springframework.beans.factory.annotation.Autowired;
                    import org.springframework.web.bind.annotation.*;
                    @RestController
                    public class UserController {
                        @Autowired
                        private UserRepository userRepository;
                        @PostMapping("/users")
                        public User create(@RequestBody CreateUserRequest request) {
                            return new User();
                        }
                    }
                    """);
            writeEntry(zipOutputStream, "src/main/resources/application.yaml", """
                    spring:
                      datasource:
                        password: root
                    """);
        }

        return outputStream.toByteArray();
    }

    private void writeEntry(ZipOutputStream zipOutputStream, String name, String content) throws Exception {
        zipOutputStream.putNextEntry(new ZipEntry(name));
        zipOutputStream.write(content.getBytes());
        zipOutputStream.closeEntry();
    }
}
