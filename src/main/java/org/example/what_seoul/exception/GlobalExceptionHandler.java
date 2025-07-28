package org.example.what_seoul.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.common.dto.CommonErrorResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;

@Hidden
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
            errorMessage = "Invalid Request Body.";
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

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<CommonErrorResponse<Object>> handleDatabaseException(DataAccessException e) {
        CommonErrorResponse<Object> errorResponse = new CommonErrorResponse<>(
          "Data Access Exception",
                "데이터 접근 중 오류가 발생했습니다."
        );
        log.error("Data Access Exception : {}", e.getMessage(), e);
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
                e.getErrors()
        );

        log.error("Validation error: {}", e.getErrors());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<CommonErrorResponse<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        CommonErrorResponse<Object> errorResponse = new CommonErrorResponse<>(
          "Data Integrity Violation",
          e.getMessage()
        );
        log.error("Data Integrity Violation Exception : {}", e.getMessage(), e);
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

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<CommonErrorResponse<Object>> handleUnauthorizedException(UnauthorizedException e) {
        CommonErrorResponse<Object> errorResponse = new CommonErrorResponse<>(
                "Unauthorized",
                e.getMessage()
        );
        log.error("Unauthorized Exception: {}", e.getMessage(), e);
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CommonErrorResponse<Object>> handleAccessDeniedException(AccessDeniedException e) {
        CommonErrorResponse<Object> errorResponse = new CommonErrorResponse<>(
                "Access Denied",
                e.getMessage()
        );
        log.error("Access Denied Exception: {}", e.getMessage(), e);
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonErrorResponse<Object>> handleGenericException(Exception e) {
        CommonErrorResponse<Object> errorResponse = new CommonErrorResponse<>(
                "Internal Server Error",
                e.getMessage()
        );
        log.error("Generic Exception : {}", e.getMessage(), e);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
