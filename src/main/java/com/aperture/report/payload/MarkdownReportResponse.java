package com.aperture.report.payload;

import java.util.UUID;

public record MarkdownReportResponse(
        UUID scanResultId,
        String markdown
) {
}
