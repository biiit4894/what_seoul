package org.example.what_seoul.controller.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResReissueAccessTokenDTO {
    @Schema(description = "재발급된 액세스 토큰의 만료 시각 (timestamp, 밀리세컨 단위)", example = "1721043562000")
    private long accessTokenExpiration;
}
