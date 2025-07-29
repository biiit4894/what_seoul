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
                                        name = "ValidationFailed",
                                        summary = "유효성 검증 실패",
                                        value = """
                                                {
                                                  "message": "Validation Failed",
                                                  "context": {
                                                    "fieldName": ["입력 필드별 유효성 검증 오류 메시지(글자수, 형식 관련)"]
                                                  },
                                                  "responseTime": "2025-07-22T15:30:45"
                                                }
                                                """
                                ),
                        }
                )
        )
})
public @interface ValidationErrorResponses {
}
