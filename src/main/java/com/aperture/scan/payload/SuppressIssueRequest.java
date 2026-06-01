package com.aperture.scan.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SuppressIssueRequest(
        @NotBlank
        @Size(max = 1000)
        String reason
) {
}
