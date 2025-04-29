package org.example.what_seoul.controller.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.user.User;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResGetUserDetailSummaryDTO {
    private Long id;
    private String userId;
    private String email;
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
