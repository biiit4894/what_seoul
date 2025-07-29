package org.example.what_seoul.controller.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReqCreateUserDTO {

    @Schema(description = "생성하려는 계정의 유저 아이디", example = "user")
    @NotBlank(message = "아이디는 필수 입력값입니다.")
    @Size(min = 4, max = 10, message = "아이디는 4자 이상 10자 이하로 입력해야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "아이디는 영어 대소문자, 숫자, '-', '_'만 포함할 수 있습니다.")
    private String userId;

    @Schema(description = "생성하려는 계정의 비밀번호", example = "%123user")
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Size(min = 4, max = 20, message = "비밀번호는 4자 이상 20자 이하로 입력해야 합니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%])[A-Za-z\\d!@#$%]+$",
            message = "비밀번호는 영어, 숫자, 특수기호(!,@,#,$,% 중 선택)를 모두 포함해야 합니다.")
    private String password;

    @Schema(description = "생성하려는 계정의 이메일 주소", example = "user@email.com")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @Schema(description = "생성하려는 계정의 닉네임", example = "테스트닉네임")
    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    @Size(min = 4, max = 20, message = "닉네임은 4자 이상 20자 이하로 입력해야 합니다.")
    @Pattern(regexp = "^[a-zA-Z가-힣0-9]+$", message = "닉네임은 영어, 한글, 숫자만 포함할 수 있습니다.")
    private String nickName;
}
