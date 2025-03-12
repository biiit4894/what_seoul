package org.example.what_seoul.common.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CommonResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime responseTime;

    public CommonResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.responseTime = LocalDateTime.now();
    }
}
