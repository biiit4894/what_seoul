package org.example.what_seoul.controller.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.domain.user.User;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResCreateAdminDTO {
    @Schema(description = "생성한 관리자 계정의 ID", example = "1")
    private Long id;

    @Schema(description = "생성한 관리자 계정의 유저 아이디", example = "test")
    private String userId;

    @Schema(description = "생성한 관리자 계정의 이메일 주소", example = "test@test.com")
    private String email;

    @Schema(description = "생성한 관리자 계정의 닉네임", example = "관리자")
    private String nickName;

    @Schema(description = "관리자 계정 생성일자", example = "2025-07-14T13:21:48")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public ResCreateAdminDTO(User user) {
        this.id = user.getId();
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.nickName = user.getNickName();
        this.createdAt = user.getCreatedAt();
    }

    public static ResCreateAdminDTO from(User user) {
        return new ResCreateAdminDTO(user);
    }
}
