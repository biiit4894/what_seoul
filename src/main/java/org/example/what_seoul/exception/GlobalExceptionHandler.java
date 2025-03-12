package org.example.what_seoul.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.example.what_seoul.common.dto.CommonErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<CommonErrorResponse<Object>> handleNoResourceFoundException(NoResourceFoundException e) {
        CommonErrorResponse<Object> errorResponse = new CommonErrorResponse<>(
                "Resource Not Found",
                e.getMessage() // TODO: 보안상 적절한 context인지 고려
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CommonErrorResponse<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        String errorMessage;

        if (e.getCause() instanceof JsonMappingException || e.getCause() instanceof JsonParseException) {
            errorMessage = "Malformed JSON request.";
        } else {
            errorMessage = "Invalid Request Body."; // TODO: 분기처리 상세화?
        }

        CommonErrorResponse<Object> errorResponse = new CommonErrorResponse<>(
                "Invalid Request",
                errorMessage
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonErrorResponse<Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        CommonErrorResponse<Object> errorResponse = new CommonErrorResponse<>(
                "Illegal Argument Exception",
                e.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonErrorResponse<Object>> handleValidationException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        StringBuilder errorMessage = new StringBuilder();
        for (FieldError fieldError : fieldErrors) {
            errorMessage.append(fieldError.getField())
                    .append(": ")
                    .append(fieldError.getDefaultMessage())
                    .append(" ");
        }

        CommonErrorResponse<Object> errorResponse = new CommonErrorResponse<>(
                "Validation Failed",
                errorMessage.toString()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateFieldException.class)
    public ResponseEntity<CommonErrorResponse<Object>> handleDuplicateFieldException(DuplicateFieldException e) {
        CommonErrorResponse<Object> errorResponse = new CommonErrorResponse<>(
                "Duplicate Field Exception",
                e.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<CommonErrorResponse<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        CommonErrorResponse<Object> errorResponse = new CommonErrorResponse<>(
          "Data Integrity Violation",
          e.getMessage() // TODO: context 반환 방식 수정
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<CommonErrorResponse<Object>> handlePasswordMismatchException(PasswordMismatchException e) {
        CommonErrorResponse<Object> errorResponse = new CommonErrorResponse<>(
                "Password Mismatch",
                e.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonErrorResponse<Object>> handleGenericException(Exception e) {
        CommonErrorResponse<Object> errorResponse = new CommonErrorResponse<>(
                "Internal Server Error",
                "An unexpected error occurred."
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
