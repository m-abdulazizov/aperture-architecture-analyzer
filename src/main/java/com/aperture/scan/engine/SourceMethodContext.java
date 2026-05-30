package com.aperture.scan.engine;

import java.util.List;

public record SourceMethodContext(
        String name,
        List<String> annotations,
        List<SourceParameterContext> parameters,
        Integer lineNumber,
        int lineCount
) {
}
