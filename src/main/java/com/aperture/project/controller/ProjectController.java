package com.aperture.project.controller;

import com.aperture.project.payload.ProjectDetailResponse;
import com.aperture.project.service.ProjectService;
import com.aperture.project.payload.ProjectCreateRequest;
import com.aperture.project.payload.ProjectCreateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController
{
    private final ProjectService projectService;

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
}