package com.aperture.scan.engine;

import java.util.List;

public record SourceParameterContext(
        String name,
        String type,
        List<String> annotations,
        Integer lineNumber
) {
}
