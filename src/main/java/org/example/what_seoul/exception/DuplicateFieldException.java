package org.example.what_seoul.exception;

import lombok.Getter;

@Getter
public class DuplicateFieldException extends RuntimeException {
    private final String fieldName;

    public DuplicateFieldException(String fieldName) {
        super(fieldName + " already exists.");
        this.fieldName = fieldName;
    }
}
