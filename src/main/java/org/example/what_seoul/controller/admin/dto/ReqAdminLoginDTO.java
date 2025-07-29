package org.example.what_seoul.controller.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReqAdminLoginDTO {
    @Schema(description = "로그인하려는 관리자 계정의 유저 아이디", example = "test")
    private String userId;

    @Schema(description = "로그인하려는 관리자 계정의 비밀번호", example = "%123abc")
    private String password;
}
