package org.example.what_seoul.controller.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReqFindPasswordDTO {
    @Schema(description = "비밀번호를 찾고자 하는 계정의 이메일 주소", example = "user@email.com")
    private String email;
}
