package org.example.what_seoul.controller.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.user.User;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResGetUserDetailDTO {
    private Long id;
    private String userId;
    private String email;
    private String nickName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static ResGetUserDetailDTO from(User user) {
        return new ResGetUserDetailDTO(
                user.getId(),
                user.getUserId(),
                user.getEmail(),
                user.getNickName(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getDeletedAt()
        );
    }
}
