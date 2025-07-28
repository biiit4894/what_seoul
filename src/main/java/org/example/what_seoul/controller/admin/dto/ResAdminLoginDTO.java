package org.example.what_seoul.controller.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResAdminLoginDTO {
    @Schema(description = "로그인한 관리자 계정의 유저 아이디", example = "test")
    private String userId;

    @Schema(description = "토큰 만료 시간 (KST, 밀리세컨 단위)", example = "1752543767000")
    private long accessTokenExpiration;
}
