package org.example.what_seoul.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class DuplicateFieldException extends RuntimeException {
    private final Map<String, String> errors;

    public DuplicateFieldException(Map<String, String> errors) {
        super("Duplicate fields found");
        this.errors = errors;
    }
}
