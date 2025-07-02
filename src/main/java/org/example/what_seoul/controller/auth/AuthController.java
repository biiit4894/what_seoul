package org.example.what_seoul.controller.auth;

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
public class AuthController {
    private final AuthService authService;

    @PostMapping("/access/reissue")
    public ResponseEntity<CommonResponse<ResReissueAccessTokenDTO>> reissueAccessToken(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.reissueAccessToken(refreshToken, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout(@CookieValue("accessToken") String accessToken, HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.logout(accessToken, response));
    }
}
