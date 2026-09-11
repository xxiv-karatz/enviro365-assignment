package com.enviro.assessment.junior.khulekani.exception;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponse {
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;
    private final String error;
    private final String message;
    private final Map<String, String> validationErrors;
    private final Object rejection;

    public ErrorResponse(int status, String error, String message) {
        this(status, error, message, null, null);
    }

    public ErrorResponse(int status, String error, String message,
                         Map<String, String> validationErrors, Object rejection) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.validationErrors = validationErrors;
        this.rejection = rejection;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }

    public Object getRejection() {
        return rejection;
    }
}
