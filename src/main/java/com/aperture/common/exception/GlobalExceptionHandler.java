package com.aperture.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler
{
    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleProjectNotFound(
            ProjectNotFoundException exception,
            WebRequest request
    ){
        return buildErrorResponse(
                exception.getMessage(),
                requestPath(request),
                HttpStatus.NOT_FOUND,
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            WebRequest request
    ){
        Map<String, String> validationErrors = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error
                -> validationErrors.put(error.getField(), error.getDefaultMessage()));

        return buildErrorResponse(
                "Validation failed",
                requestPath(request),
                HttpStatus.BAD_REQUEST,
                validationErrors
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception exception,
            WebRequest request
    ){
        return buildErrorResponse(
                exception.getMessage(),
                requestPath(request),
                HttpStatus.INTERNAL_SERVER_ERROR,
                null
        );
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(
            String message,
            String path,
            HttpStatus status,
            Map<String, String> validationErrors
    ){
        ApiErrorResponse response = new ApiErrorResponse(
                message,
                path,
                status.value(),
                status.getReasonPhrase(),
                LocalDateTime.now(),
                validationErrors
        );
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidFile(
            InvalidFileException exception,
            WebRequest request
    ) {
        return buildErrorResponse(
                exception.getMessage(),
                requestPath(request),
                HttpStatus.BAD_REQUEST,
                null
        );
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ApiErrorResponse> handleStorage(
            StorageException exception,
            WebRequest request
    ) {
        return buildErrorResponse(
                exception.getMessage(),
                requestPath(request),
                HttpStatus.INTERNAL_SERVER_ERROR,
                null
        );
    }

    @ExceptionHandler(ScanFailedException.class)
    public ResponseEntity<ApiErrorResponse> handleScanFailed(
            ScanFailedException exception,
            WebRequest request
    ) {
        return buildErrorResponse(
                exception.getMessage(),
                requestPath(request),
                HttpStatus.INTERNAL_SERVER_ERROR,
                null
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception,
            WebRequest request
    ) {
        return buildErrorResponse(
                exception.getMessage(),
                requestPath(request),
                HttpStatus.BAD_REQUEST,
                null
        );
    }

    private String requestPath(WebRequest request) {
        String description = request.getDescription(false);
        if (description.startsWith("uri=")) {
            return description.substring(4);
        }
        return description;
    }
}
