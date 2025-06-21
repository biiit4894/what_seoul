package org.example.what_seoul.controller.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.user.User;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResCreateAdminDTO {
    private Long id;
    private String userId;
    private String email;
    private String nickName;
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
