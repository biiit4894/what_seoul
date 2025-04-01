package org.example.what_seoul.controller.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.user.User;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResCreateUserDTO {
    private Long id;
    private String userId;
    private String email;
    private String nickName;
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
