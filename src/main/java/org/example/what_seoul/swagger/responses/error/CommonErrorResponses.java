package org.example.what_seoul.swagger.responses.error;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = @Content(
                        mediaType = "application/json",
                        examples = {
                                @ExampleObject(
                                        name = "InvalidRequest",
                                        summary = "잘못된 요청 형식",
                                        value = """
                                                {
                                                  "message": "Invalid Request",
                                                  "context": "Malformed JSON request.",
                                                  "responseTime": "2025-07-22T15:30:45"
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "MissingParameter",
                                        summary = "필수 파라미터 누락",
                                        value = """
                                                {
                                                  "message": "Missing Request Parameter",
                                                  "context": "필수 파라미터 'parameterName'가 누락되었습니다.",
                                                  "responseTime": "2025-07-22T15:30:45"
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "TypeMismatch",
                                        summary = "타입 불일치",
                                        value = """
                                                {
                                                  "message": "Type Mismatch",
                                                  "context": "'value' 값을 'requiredType' 타입으로 변환할 수 없습니다.",
                                                  "responseTime": "2025-07-22T15:30:45"
                                                }
                                                """
                                )
                        }
                )
        ),
        @ApiResponse(
                responseCode = "401",
                description = "인증 실패",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "Unauthorized",
                                summary = "인증 토큰 없음 또는 만료",
                                value = """
                                        {
                                          "message": "Unauthorized",
                                          "context": "유효하지 않거나 만료된 토큰입니다.",
                                          "responseTime": "2025-07-22T15:30:45"
                                        }
                                        """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "403",
                description = "권한 없음",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "AccessDenied",
                                summary = "접근 권한 없음",
                                value = """
                                        {
                                          "message": "Forbidden",
                                          "context": "접근 권한이 없습니다.",
                                          "responseTime": "2025-07-22T15:30:45"
                                        }
                                        """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "리소스를 찾을 수 없음",
                content = @Content(
                        mediaType = "application/json",
                        examples = {
                                @ExampleObject(
                                        name = "ResourceNotFound",
                                        summary = "요청한 리소스 없음",
                                        value = """
                                                {
                                                  "message": "Resource Not Found",
                                                  "context": "요청한 리소스를 찾을 수 없습니다.",
                                                  "responseTime": "2025-07-22T15:30:45"
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "EntityNotFound",
                                        summary = "데이터 조회 실페",
                                        value = """
                                                {
                                                  "message": "Entity Not Found",
                                                  "context": "'dataName(ex.사용자, 문화행사 후기)'을 찾을 수 없습니다.",
                                                  "responseTime": "2025-07-22T15:30:45"
                                                }
                                                """
                                )
                        }
                )
        ),
        @ApiResponse(
                responseCode = "405",
                description = "허용되지 않는 HTTP 메서드",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "MethodNotAllowed",
                                summary = "지원하지 않는 HTTP 메서드",
                                value = """
                                        {
                                          "message": "Method Not Allowed",
                                          "context": "Request method 'methodName(ex. DELETE)' not supported",
                                          "responseTime": "2025-07-22T15:30:45"
                                        }
                                        """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = @Content(
                        mediaType = "application/json",
                        examples = {
                                @ExampleObject(
                                        name = "InternalServerError",
                                        summary = "일반적인 서버 오류",
                                        value = """
                                                {
                                                  "message": "Internal Server Error",
                                                  "context": "서버 에러 관련 메시지",
                                                  "responseTime": "2025-07-22T15:30:45"
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "DataAccessException",
                                        summary = "데이터베이스 접근 오류",
                                        value = """
                                                {
                                                  "message": "Data Access Exception",
                                                  "context": "데이터 접근 중 오류가 발생했습니다.",
                                                  "responseTime": "2025-07-22T15:30:45"
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "CitydataSchedulerError",
                                        summary = "도시데이터 저장 및 업데이트 스케줄러 오류",
                                        value = """
                                                {
                                                  "message": "Citydata Scheduler Error",
                                                  "context": "스케줄러 작업 중 오류가 발생했습니다.",
                                                  "responseTime": "2025-07-22T15:30:45"
                                                }
                                                """
                                )
                        }
                )
        )
})
public @interface CommonErrorResponses {
}
