package org.example.what_seoul.controller.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResAdminLoginDTO {
    private String userId;
    private String accessToken;
    private String refreshToken;
}
