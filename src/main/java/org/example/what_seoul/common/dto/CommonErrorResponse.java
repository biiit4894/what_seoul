package org.example.what_seoul.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 공통 오류 응답을 나타내는 DTO 클래스.
 * 이 클래스는 API 오류 응답에서 사용할 메시지, 설명, 응답 시간을 포함한다.
 *
 * - `message`: 시스템/에러 코드의 고정된 식별자 (영문) (ex: "Resource Not Found", "Validation Failed", "Unauthorized Access" 등)
 * - `context`: 사용자 또는 개발자를 위한 설명 메시지 (한글) (ex: "해당 리소스를 찾을 수 없습니다.", "입력값이 올바르지 않습니다.")
 * - `responseTime`: 오류 발생 시간. API 응답 시간 정보를 포함하여 사용자가 언제 해당 오류가 발생했는지를 알 수 있게 한다.
 */
@Getter
@NoArgsConstructor
public class CommonErrorResponse<T> {
    @Schema(description = "시스템 및 에러 메시지(영문)", example = "\"Resource Not Found\", \"Validation Failed\", \"Unauthorized Access\" 등")
    private String message;

    @Schema(description = "사용자 또는 개발자를 위한 설명 메시지", example = "\"해당 리소스를 찾을 수 없습니다.\", \"입력값이 올바르지 않습니다.\" 등")
    private T context;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "오류 발생 시간. API 응답 시간 정보를 포함하여 사용자가 언제 해당 오류가 발생했는지를 알 수 있게 한다.", example = "2025-07-15T12:24:51")
    private LocalDateTime responseTime;

    public CommonErrorResponse(String message, T context) {
        this.message = message;
        this.context = context;
        this.responseTime = LocalDateTime.now();
    }

}
