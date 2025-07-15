package org.example.what_seoul.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.auth.dto.ResReissueAccessTokenDTO;
import org.example.what_seoul.service.auth.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "유저 유형에 관계 없이 사용되는 로그아웃, 토큰 재발급 기능입니다.")
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "액세스 토큰 재발급",
            description = """
            Refresh Token을 이용해 Access Token을 재발급합니다.\s
            - Refresh Token은 쿠키에 담겨 전달되어야 합니다. (refreshToken 쿠키 사용)\s
            - 재발급된 Access Token은 쿠키에 담아 응답됩니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공", content = @Content(
                    schema = @Schema(implementation = ResReissueAccessTokenDTO.class),
                    examples = @ExampleObject(
                            value = """
                            {
                              "success": true,
                              "message": "액세스 토큰 재발급 성공",
                              "data": {
                                "accessTokenExpiration": 1721043562000
                              },
                              "responseTime": "2025-07-15T12:00:00.000Z"
                            }
                            """
                    )
            )),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    })
    @PostMapping("/access/reissue")
    public ResponseEntity<CommonResponse<ResReissueAccessTokenDTO>> reissueAccessToken(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.reissueAccessToken(refreshToken, response));
    }

    @Operation(
            summary = "로그아웃",
            description = """
            현재 로그인된 사용자의 토큰을 무효화하고 쿠키에서 제거합니다.
            - Access Token은 accessToken 쿠키를 통해 전달되어야 합니다.
            - Redis에 저장된 Refresh Token도 삭제됩니다.
            - AccessToken, RefreshToken 쿠키 모두 즉시 만료 처리됩니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공", content = @Content(
                    examples = @ExampleObject(
                            value = """
                    {
                      "success": true,
                      "message": "로그아웃 성공",
                      "data": null,
                      "responseTime": "2025-07-15T12:00:00.000Z"
                    }
                    """
                    )
            )),
            @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 Access Token")
    })
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout(@CookieValue("accessToken") String accessToken, HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.logout(accessToken, response));
    }
}
