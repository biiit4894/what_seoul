package org.example.what_seoul.exception;

public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException() {
        super("Incorrect Current Password");
    }
}
