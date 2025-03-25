package org.example.what_seoul.controller.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetUserSummaryRes {
    private Long id;
    private String userId;
    private String email;
    private String nickName;
}
