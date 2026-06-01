package com.aperture.scan.payload;

public record ArchitectureEdgeResponse(
        String source,
        String target,
        String type
) {
}
