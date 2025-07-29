package org.example.what_seoul.controller.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.user.User;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResCreateUserDTO {
    @Schema(description = "생성한 계정의 ID", example = "1")
    private Long id;

    @Schema(description = "생성한 계정의 유저 아이디", example = "user")
    private String userId;

    @Schema(description = "생성한 계정의 이메일 주소", example = "user@email.com")
    private String email;

    @Schema(description = "생성한 계정의 닉네임", example = "테스트닉네임")
    private String nickName;

    @Schema(description = "계정 생성일자", example = "2025-05-03T15:03:18")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public ResCreateUserDTO(User user) {
        this.id = user.getId();
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.nickName = user.getNickName();
        this.createdAt = user.getCreatedAt();
    }

    public static ResCreateUserDTO from(User user) {
        return new ResCreateUserDTO(user);
    }
}
