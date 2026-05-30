package com.aperture.storage.payload;

public record StoredFileInfo(
        String originalFileName,
        String storedPath,
        long size
) {
}