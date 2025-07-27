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
public class ResUpdateUserDTO {
    @Schema(description = "계정 ID", example = "1")
    private Long id;

    @Schema(description = "회원정보 수정 후 유저 아이디", example = "user")
    private String userId;

    @Schema(description = "회원정보 수정 후 이메일 주소", example = "newemail@email.ccom")
    private String email;

    @Schema(description = "회원정보 수정 후 닉네임", example = "신규닉네임")
    private String nickName;

    @Schema(description = "계정 생성일자", example = "2025-05-03T15:03:18")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "계정 수정일자", example = "2025-06-25T08:22:27")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public ResUpdateUserDTO(User user) {
        this.id = user.getId();
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.nickName = user.getNickName();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }

    public static ResUpdateUserDTO from(User user) {
        return new ResUpdateUserDTO(user);
    }
}
