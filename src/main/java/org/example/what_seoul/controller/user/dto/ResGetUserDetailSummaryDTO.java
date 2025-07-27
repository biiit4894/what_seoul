package org.example.what_seoul.controller.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.user.User;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResGetUserDetailSummaryDTO {
    @Schema(description = "조회한 계정의 ID", example = "1")
    private Long id;

    @Schema(description = "조회한 계정의 유저 아이디", example = "user")
    private String userId;

    @Schema(description = "조회한 계정의 이메일 주소", example = "user@email.com")
    private String email;

    @Schema(description = "조회한 계정의 닉네임", example = "테스트닉네임")
    private String nickName;

    public static ResGetUserDetailSummaryDTO from(User user) {
        return new ResGetUserDetailSummaryDTO(
                user.getId(),
                user.getUserId(),
                user.getEmail(),
                user.getNickName()
        );
    }
}
