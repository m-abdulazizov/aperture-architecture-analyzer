package com.aperture.scan.entity;

public enum ScanJobStage {
    QUEUED,
    DISCOVERING_FILES,
    PARSING_SOURCE,
    RUNNING_RULES,
    SAVING_REPORT,
    COMPLETED,
    FAILED
}
