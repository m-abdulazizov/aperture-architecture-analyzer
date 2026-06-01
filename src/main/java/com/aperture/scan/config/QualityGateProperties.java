package com.aperture.scan.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aperture.quality-gate")
public class QualityGateProperties {

    private int minimumScore = 80;
    private int maxCriticalIssues = 0;
    private int maxHighIssues = 5;

    public int getMinimumScore() {
        return minimumScore;
    }

    public void setMinimumScore(int minimumScore) {
        this.minimumScore = minimumScore;
    }

    public int getMaxCriticalIssues() {
        return maxCriticalIssues;
    }

    public void setMaxCriticalIssues(int maxCriticalIssues) {
        this.maxCriticalIssues = maxCriticalIssues;
    }

    public int getMaxHighIssues() {
        return maxHighIssues;
    }

    public void setMaxHighIssues(int maxHighIssues) {
        this.maxHighIssues = maxHighIssues;
    }
}
