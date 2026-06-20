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
        mockMvc.perform(post("/api/v1/projects")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Unauthorized REST Flow",
                                  "description": "Should require a token"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        String token = registerAndToken();
        String projectBody = """
                {
                  "name": "REST Flow %s",
                  "description": "Integration test project"
                }
                """.formatted(UUID.randomUUID());

        JsonNode project = objectMapper.readTree(mockMvc.perform(post("/api/v1/projects")
                        .contentType("application/json")
                        .header("Authorization", bearer(token))
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
                        .file(archive)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        JsonNode scan = objectMapper.readTree(mockMvc.perform(post("/api/v1/projects/{projectId}/scan", projectId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());

        String scanResultId = scan.get("id").asText();
        assertThat(scan.get("totalIssues").asInt()).isGreaterThan(0);

        JsonNode filteredIssues = objectMapper.readTree(mockMvc.perform(get("/api/v1/scan-results/{scanResultId}/issues", scanResultId)
                        .header("Authorization", bearer(token))
                        .param("severity", "CRITICAL")
                        .param("category", "SECURITY"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());

        assertThat(filteredIssues.get("content")).hasSize(1);

        String firstIssueId = filteredIssues.get("content").get(0).get("id").asText();
        mockMvc.perform(post("/api/v1/issues/{issueId}/suppressions", firstIssueId)
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content("""
                                {
                                  "reason": "Known demo issue"
                                }
                                """))
                .andExpect(status().isOk());

        JsonNode suppressions = objectMapper.readTree(mockMvc.perform(get("/api/v1/projects/{projectId}/suppressions", projectId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());

        assertThat(suppressions).hasSize(1);

        JsonNode report = objectMapper.readTree(mockMvc.perform(get("/api/v1/scan-results/{scanResultId}/report/json", scanResultId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());

        assertThat(report.get("issues")).hasSize(scan.get("totalIssues").asInt());
        assertThat(report.get("issuesByCategory")).isNotEmpty();

        JsonNode sarif = objectMapper.readTree(mockMvc.perform(get("/api/v1/scan-results/{scanResultId}/report/sarif", scanResultId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());

        assertThat(sarif.get("version").asText()).isEqualTo("2.1.0");
        assertThat(sarif.get("runs").get(0).get("results")).isNotEmpty();

        JsonNode qualityGate = objectMapper.readTree(mockMvc.perform(get("/api/v1/scan-results/{scanResultId}/quality-gate", scanResultId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());

        assertThat(qualityGate.get("passed").asBoolean()).isFalse();
    }

    private String registerAndToken() throws Exception {
        JsonNode response = objectMapper.readTree(mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "scan-flow-%s@example.com",
                                  "password": "password123"
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());

        return response.get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
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
