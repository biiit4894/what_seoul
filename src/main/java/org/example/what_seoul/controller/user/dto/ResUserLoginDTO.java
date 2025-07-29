package org.example.what_seoul.controller.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResUserLoginDTO {
    @Schema(description = "로그인한 일반 유저 계정의 유저 아이디", example = "user")
    private String userId;

    @Schema(description = "토큰 만료 시간 (KST, 밀리세컨 단위)", example = "1752543767000")
    private long accessTokenExpiration;
}
