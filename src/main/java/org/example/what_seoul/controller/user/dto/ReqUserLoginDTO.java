package org.example.what_seoul.controller.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReqUserLoginDTO {
    @Schema(description = "로그인하려는 계정의 유저 아이디", example = "user")
    private String userId;

    @Schema(description = "로그인하려는 계정의 비밀번호", example = "%123user")
    private String password;
}
