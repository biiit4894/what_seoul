package org.example.what_seoul.exception;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class CustomValidationException extends RuntimeException {
    private final Map<String, List<String>> errors;

    public CustomValidationException(Map<String, List<String>> errors) {
        super("Validation failed.");
        this.errors = errors;
    }
}
