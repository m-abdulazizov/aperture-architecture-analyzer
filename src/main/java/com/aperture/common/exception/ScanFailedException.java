package com.aperture.common.exception;

public class ScanFailedException extends RuntimeException {

    public ScanFailedException(String message) {
        super(message);
    }

    public ScanFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
