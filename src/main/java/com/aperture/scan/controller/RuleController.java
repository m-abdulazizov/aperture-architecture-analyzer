package com.aperture.scan.controller;

import com.aperture.scan.payload.RuleMetadataResponse;
import com.aperture.scan.service.RuleCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleCatalogService ruleCatalogService;

    @GetMapping
    public ResponseEntity<List<RuleMetadataResponse>> getRules() {
        return ResponseEntity.ok(ruleCatalogService.getRules());
    }
}
