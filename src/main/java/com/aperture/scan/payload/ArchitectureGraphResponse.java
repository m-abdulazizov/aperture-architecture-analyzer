package com.aperture.scan.payload;

import java.util.List;
import java.util.UUID;

public record ArchitectureGraphResponse(
        UUID projectId,
        List<String> nodes,
        List<ArchitectureEdgeResponse> edges
) {
}
