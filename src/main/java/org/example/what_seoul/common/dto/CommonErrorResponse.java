package org.example.what_seoul.common.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CommonErrorResponse<T> {
    private String message;
    private T context;
    private LocalDateTime responseTime;

    public CommonErrorResponse(String message, T context) {
        this.message = message;
        this.context = context;
        this.responseTime = LocalDateTime.now();
    }

}
