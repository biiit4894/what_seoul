package org.example.what_seoul.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.auth.dto.ResReissueAccessTokenDTO;
import org.example.what_seoul.service.auth.AuthService;
import org.example.what_seoul.swagger.operation.description.auth.AuthDescription;
import org.example.what_seoul.swagger.responses.error.CommonErrorResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "유저 유형에 관계 없이 사용되는 로그아웃, 토큰 재발급 기능입니다.")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "액세스 토큰 재발급", description = AuthDescription.REISSUE_ACCESS_TOKEN)
    @CommonErrorResponses
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공")
    })
    @PostMapping("/access/reissue")
    public ResponseEntity<CommonResponse<ResReissueAccessTokenDTO>> reissueAccessToken(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.reissueAccessToken(refreshToken, response));
    }

    @Operation(summary = "로그아웃", description = AuthDescription.LOGOUT)
    @CommonErrorResponses
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    })
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout(@CookieValue("accessToken") String accessToken, HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.logout(accessToken, response));
    }
}
