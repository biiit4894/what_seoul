package org.example.what_seoul.controller.auth;

import lombok.RequiredArgsConstructor;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.auth.dto.ResReissueAccessTokenDTO;
import org.example.what_seoul.service.auth.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/access/reissue")
    public ResponseEntity<CommonResponse<ResReissueAccessTokenDTO>> reissueAccessToken(@RequestHeader("Authorization") String token) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.reissueAccessToken(token));
    }
}
