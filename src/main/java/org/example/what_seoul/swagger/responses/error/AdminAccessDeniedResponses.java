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
                responseCode = "403",
                description = "관리자 권한 없음",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "AccessDenied",
                                summary = "관리자 권한 없음",
                                value = """
                                        {
                                          "message": "Access Denied",
                                          "context": "관리자 권한이 없습니다.",
                                          "responseTime": "2025-07-22T15:30:45"
                                        }
                                        """
                        )
                )
        )
})
public @interface AdminAccessDeniedResponses {
}
