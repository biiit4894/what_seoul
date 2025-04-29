package org.example.what_seoul.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CommonErrorResponse<T> {
    private String message;
    private T context;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime responseTime;

    public CommonErrorResponse(String message, T context) {
        this.message = message;
        this.context = context;
        this.responseTime = LocalDateTime.now();
    }

}
