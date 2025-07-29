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
public class ReqUpdateUserInfoDTO {
    @Schema(description = "회원정보를 수정하고자 하는 계정의 현재 비밀번호", example = "%123user")
    @NotBlank(message = "기존 비밀번호는 필수로 입력해야 합니다.")
    private String currPassword;

    @Schema(description = "신규 비밀번호", example = "%123newpassword")
    @Size(min=4, max=20, message="4자 이상 20자 이하이어야 합니다.")
    // 영어(대소문자) 최소 1개, 숫자 최소 1개, 특수문자 최소 1개 포함
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-])[A-Za-z\\d!@#$%^&*()_+=-]+$", message = "영어, 숫자, 특수기호를 모두 포함해야 합니다.")
    private String newPassword;

    @Schema(description = "신규 이메일 주소", example = "newemail@email.ccom")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "example@mail.com과 같은 이메일 형식을 준수해야 합니다.")
    private String newEmail;

    @Schema(description = "신규 닉네임", example = "신규닉네임")
    @Size(min=4, max=20, message="4자 이상 20자 이하이어야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]*$", message="영어 대소문자, 한글, 또는 숫자만을 사용할 수 있습니다.")
    private String newNickName;
}
