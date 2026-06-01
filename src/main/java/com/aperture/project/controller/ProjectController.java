package com.aperture.project.controller;

import com.aperture.project.payload.ProjectDetailResponse;
import com.aperture.project.payload.ProjectStatsResponse;
import com.aperture.project.payload.SampleZipResponse;
import com.aperture.project.service.SampleProjectService;
import com.aperture.project.service.ProjectService;
import com.aperture.project.payload.ProjectCreateRequest;
import com.aperture.project.payload.ProjectCreateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.aperture.project.payload.ProjectUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController
{
    private final ProjectService projectService;
    private final SampleProjectService sampleProjectService;

    @PostMapping
    public ResponseEntity<ProjectCreateResponse> create(@Valid @RequestBody ProjectCreateRequest request){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(projectService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDetailResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<ProjectDetailResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(projectService.getAll(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/upload")
    public ResponseEntity<ProjectUploadResponse> upload(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(projectService.upload(id, file));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<ProjectStatsResponse> getStats(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getStats(id));
    }

    @PostMapping("/samples/vulnerable-spring/package")
    public ResponseEntity<SampleZipResponse> packageVulnerableSample() {
        return ResponseEntity.ok(sampleProjectService.packageVulnerableSample());
    }
}
