package org.example.what_seoul.common.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CommonErrorResponse<RequestType> {
    private String message;
    private String context;
    private LocalDateTime responseTime;

    public CommonErrorResponse(String message, String context) {
        this.message = message;
        this.context = context;
        this.responseTime = LocalDateTime.now();
    }

}
