package com.aperture.scan.service;

import com.aperture.scan.config.RuleProperties;
import com.aperture.scan.entity.IssueSeverity;
import com.aperture.scan.entity.RuleConfiguration;
import com.aperture.scan.payload.RuleConfigurationRequest;
import com.aperture.scan.payload.RuleConfigurationResponse;
import com.aperture.scan.repository.RuleConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RuleConfigurationService {

    private final RuleProperties ruleProperties;
    private final RuleConfigurationRepository ruleConfigurationRepository;

    @Transactional(readOnly = true)
    public boolean isEnabled(UUID projectId, String ruleCode) {
        RuleConfiguration configuration = findEffectiveConfiguration(projectId, ruleCode);
        if (configuration != null) {
            return configuration.isEnabled();
        }

        return !ruleProperties.getDisabled().contains(ruleCode);
    }

    @Transactional(readOnly = true)
    public IssueSeverity severityFor(UUID projectId, String ruleCode, IssueSeverity defaultSeverity) {
        RuleConfiguration configuration = findEffectiveConfiguration(projectId, ruleCode);
        if (configuration != null && configuration.getSeverityOverride() != null) {
            return configuration.getSeverityOverride();
        }

        return ruleProperties.getSeverityOverrides().getOrDefault(ruleCode, defaultSeverity);
    }

    @Transactional(readOnly = true)
    public List<RuleConfigurationResponse> getProjectConfigurations(UUID projectId) {
        return ruleConfigurationRepository.findAllByProjectIdIsNullOrProjectId(projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public RuleConfigurationResponse updateProjectConfiguration(UUID projectId, String ruleCode, RuleConfigurationRequest request) {
        RuleConfiguration configuration = ruleConfigurationRepository.findByProjectIdAndRuleCode(projectId, ruleCode)
                .orElseGet(() -> RuleConfiguration.builder()
                        .projectId(projectId)
                        .ruleCode(ruleCode)
                        .enabled(true)
                        .build());

        if (request.enabled() != null) {
            configuration.setEnabled(request.enabled());
        }
        configuration.setSeverityOverride(request.severityOverride());

        return toResponse(ruleConfigurationRepository.save(configuration));
    }

    private RuleConfiguration findEffectiveConfiguration(UUID projectId, String ruleCode) {
        if (projectId != null) {
            var projectConfiguration = ruleConfigurationRepository.findByProjectIdAndRuleCode(projectId, ruleCode);
            if (projectConfiguration.isPresent()) {
                return projectConfiguration.get();
            }
        }

        return ruleConfigurationRepository.findByProjectIdIsNullAndRuleCode(ruleCode).orElse(null);
    }

    private RuleConfigurationResponse toResponse(RuleConfiguration configuration) {
        return new RuleConfigurationResponse(
                configuration.getId(),
                configuration.getProjectId(),
                configuration.getRuleCode(),
                configuration.isEnabled(),
                configuration.getSeverityOverride()
        );
    }
}
