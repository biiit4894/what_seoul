package org.example.what_seoul.controller.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReqWithdrawUserDTO {
    @Schema(description = "탈퇴처리하고자 하는 계정의 현재 비밀번호", example = "%123user")
    private String password;
}
