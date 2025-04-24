package org.example.what_seoul.exception;

public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException() {
        super("정확한 비밀번호를 입력해 주세요.");
    }
}
