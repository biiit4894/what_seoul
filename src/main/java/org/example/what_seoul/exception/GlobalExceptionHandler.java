package org.example.what_seoul.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.common.dto.CommonErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<CommonErrorResponse<Object>> handleNoResourceFoundException(NoResourceFoundException e) {
        CommonErrorResponse<Object> errorResponse = new CommonErrorResponse<>(
                "Resource Not Found",
                "요청한 리소스를 찾을 수 없습니다."
        );
        log.error("No Resource Found Exception : {}", e.getMessage(), e);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CommonErrorResponse<Object>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        CommonErrorResponse<Object> errorResponse = new CommonErrorResponse<>(
                "Method Not Allowed",
                e.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
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
        log.error("Http Message Not Readable Exception : {}", e.getMessage(), e);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonErrorResponse<Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        CommonErrorResponse<Object> errorResponse = new CommonErrorResponse<>(
                "Illegal Argument Exception",
                e.getMessage()
        );
        log.error("Illegal Argument Exception : {}", e.getMessage(), e);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CommonErrorResponse<Object>> handleMissingRequestParam(MissingServletRequestParameterException e) {
        CommonErrorResponse<Object> errorResponse = new CommonErrorResponse<>(
                "Missing Request Parameter",
                String.format("필수 파라미터 '%s'가 누락되었습니다.", e.getParameterName())
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<CommonErrorResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        CommonErrorResponse<Object> errorResponse = new CommonErrorResponse<>(
                "Type Mismatch",
                String.format("'%s' 값을 '%s' 타입으로 변환할 수 없습니다.", e.getValue(), e.getRequiredType().getSimpleName())
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<CommonErrorResponse<Object>> handleDatabaseException(DatabaseException e) {
        CommonErrorResponse<Object> errorResponse = new CommonErrorResponse<>(
          "Database Exception",
                e.getMessage()
        );
        log.error("Database Exception : {}", e.getMessage(), e);

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<CommonErrorResponse<Object>> handleEntityNotFoundException(EntityNotFoundException e) {
        CommonErrorResponse<Object> errorResponse = new CommonErrorResponse<>(
                "Entity Not Found",
                e.getMessage()
        );
        log.error("Entity Not Found Exception : {}", e.getMessage(), e);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CustomValidationException.class)
    public ResponseEntity<CommonErrorResponse<Map<String, List<String>>>> handleCustomValidationException(CustomValidationException e) {
        CommonErrorResponse<Map<String, List<String>>> errorResponse = new CommonErrorResponse<>(
                "Validation Failed",
                e.getErrors()  // 유효성 검증 및 중복 오류 메시지 반환
        );

        log.error("Validation error: {}", e.getErrors());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<CommonErrorResponse<Object>> handleValidationException(MethodArgumentNotValidException e) {
//        Map<String, List<String>> errors = new HashMap<>();
//        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
//            errors.computeIfAbsent(fieldError.getField(), key -> new ArrayList<>()).add(fieldError.getDefaultMessage());
//
//        }
//
//        CommonErrorResponse<Object> errorResponse = new CommonErrorResponse<>(
//                "Validation Failed",
//                errors
//        );
//        log.error("Validation errors: {}", errors);
//
//        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
//    }

    @ExceptionHandler(DuplicateFieldException.class)
    public ResponseEntity<CommonErrorResponse<Map<String, String>>> handleDuplicateFieldException(DuplicateFieldException e) {
        CommonErrorResponse<Map<String, String>> errorResponse = new CommonErrorResponse<>(
                "Duplicate Fields Found",
                e.getErrors()
        );

        log.error("Duplicate field error: {}", e.getErrors());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<CommonErrorResponse<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        CommonErrorResponse<Object> errorResponse = new CommonErrorResponse<>(
          "Data Integrity Violation",
          e.getMessage() // TODO: context 반환 방식 수정
        );
        log.error("Data Integrity Violation Exception : {}", e.getMessage(), e);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<CommonErrorResponse<Object>> handlePasswordMismatchException(PasswordMismatchException e) {
        CommonErrorResponse<Object> errorResponse = new CommonErrorResponse<>(
                "Password Mismatch",
                e.getMessage()
        );
        log.error("Password Mismatch Exception : {}", e.getMessage(), e);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CitydataSchedulerException.class)
    public ResponseEntity<CommonErrorResponse<Object>> handleCitydataSchedulerException(CitydataSchedulerException e) {
        CommonErrorResponse<Object> errorResponse = new CommonErrorResponse<>(
                "Citydata Scheduler Error",
                e.getMessage()
        );
        log.error("Citydata Scheduler Exception : {}", e.getMessage(), e);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonErrorResponse<Object>> handleGenericException(Exception e) {
        CommonErrorResponse<Object> errorResponse = new CommonErrorResponse<>(
                "Internal Server Error",
                e.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
