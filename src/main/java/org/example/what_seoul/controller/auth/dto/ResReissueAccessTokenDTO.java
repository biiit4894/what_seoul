package org.example.what_seoul.controller.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResReissueAccessTokenDTO {
    private long accessTokenExpiration;
}
