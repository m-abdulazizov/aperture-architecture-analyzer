package com.aperture.scan.controller;

import com.aperture.scan.payload.RuleMetadataResponse;
import com.aperture.scan.payload.RuleDocumentationResponse;
import com.aperture.scan.payload.RuleConfigurationRequest;
import com.aperture.scan.payload.RuleConfigurationResponse;
import com.aperture.scan.service.RuleConfigurationService;
import com.aperture.scan.service.RuleCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleCatalogService ruleCatalogService;
    private final RuleConfigurationService ruleConfigurationService;

    @GetMapping
    public ResponseEntity<List<RuleMetadataResponse>> getRules() {
        return ResponseEntity.ok(ruleCatalogService.getRules());
    }

    @GetMapping("/{ruleCode}")
    public ResponseEntity<RuleMetadataResponse> getRule(@PathVariable String ruleCode) {
        return ResponseEntity.ok(ruleCatalogService.getRule(ruleCode));
    }

    @GetMapping("/{ruleCode}/docs")
    public ResponseEntity<RuleDocumentationResponse> getRuleDocumentation(@PathVariable String ruleCode) {
        return ResponseEntity.ok(new RuleDocumentationResponse(ruleCode, ruleCatalogService.getRuleDocumentation(ruleCode)));
    }

    @GetMapping("/projects/{projectId}/config")
    public ResponseEntity<List<RuleConfigurationResponse>> getProjectRuleConfigurations(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ruleConfigurationService.getProjectConfigurations(projectId));
    }

    @PutMapping("/projects/{projectId}/config/{ruleCode}")
    public ResponseEntity<RuleConfigurationResponse> updateProjectRuleConfiguration(
            @PathVariable UUID projectId,
            @PathVariable String ruleCode,
            @RequestBody RuleConfigurationRequest request
    ) {
        return ResponseEntity.ok(ruleConfigurationService.updateProjectConfiguration(projectId, ruleCode, request));
    }
}
