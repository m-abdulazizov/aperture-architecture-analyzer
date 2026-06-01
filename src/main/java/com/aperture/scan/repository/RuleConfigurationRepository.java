package com.aperture.scan.repository;

import com.aperture.scan.entity.RuleConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RuleConfigurationRepository extends JpaRepository<RuleConfiguration, UUID> {

    List<RuleConfiguration> findAllByProjectIdIsNullOrProjectId(UUID projectId);

    Optional<RuleConfiguration> findByProjectIdAndRuleCode(UUID projectId, String ruleCode);

    Optional<RuleConfiguration> findByProjectIdIsNullAndRuleCode(String ruleCode);
}
