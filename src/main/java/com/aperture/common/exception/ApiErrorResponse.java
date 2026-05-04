package com.aperture.common.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiErrorResponse(
        String message,
        String path,
        int status,
        String error,
        LocalDateTime timestamp,
        Map<String, String> validationErrors
) {}
