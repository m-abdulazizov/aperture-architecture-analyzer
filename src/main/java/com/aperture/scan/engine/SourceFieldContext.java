package com.aperture.scan.engine;

import java.util.List;

public record SourceFieldContext(
        String name,
        String type,
        List<String> annotations,
        Integer lineNumber
) {
}
