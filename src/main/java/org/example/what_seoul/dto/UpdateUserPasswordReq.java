package org.example.what_seoul.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserPasswordReq {
    @NotBlank
    private String currPassword;

    @NotBlank
    @Size(min=4, max=20, message="4자 이상 20자 이하이어야 합니다.")
    // 영어(대소문자) 최소 1개, 숫자 최소 1개, 특수문자 최소 1개 포함
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-])[A-Za-z\\d!@#$%^&*()_+=-]+$", message = "영어, 숫자, 특수기호를 모두 포함해야 합니다.")
    private String newPassword;
}
