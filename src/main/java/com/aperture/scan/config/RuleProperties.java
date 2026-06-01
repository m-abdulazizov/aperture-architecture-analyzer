package com.aperture.scan.config;

import com.aperture.scan.entity.IssueSeverity;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@ConfigurationProperties(prefix = "aperture.rules")
public class RuleProperties {

    private Set<String> disabled = new HashSet<>();
    private Map<String, IssueSeverity> severityOverrides = new HashMap<>();

    public Set<String> getDisabled() {
        return disabled;
    }

    public void setDisabled(Set<String> disabled) {
        this.disabled = disabled;
    }

    public Map<String, IssueSeverity> getSeverityOverrides() {
        return severityOverrides;
    }

    public void setSeverityOverrides(Map<String, IssueSeverity> severityOverrides) {
        this.severityOverrides = severityOverrides;
    }
}
