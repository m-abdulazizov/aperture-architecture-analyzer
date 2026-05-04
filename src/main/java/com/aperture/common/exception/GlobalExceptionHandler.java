package com.aperture.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler
{
    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleProjectNotFound(
            ProjectNotFoundException exception,
            HttpServletRequest request
    ){
        return buildErrorResponse(
                exception.getMessage(),
                request.getRequestURI(),
                HttpStatus.NOT_FOUND,
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ){
        Map<String, String> validationErrors = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error
                -> validationErrors.put(error.getField(), error.getDefaultMessage()));

        return buildErrorResponse(
                "Validation failed",
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST,
                validationErrors
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception exception,
            HttpServletRequest request
    ){
        return buildErrorResponse(
                exception.getMessage(),
                request.getRequestURI(),
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
}
