package org.example.what_seoul.service.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.config.JwtTokenProvider;
import org.example.what_seoul.controller.auth.dto.ResReissueAccessTokenDTO;
import org.example.what_seoul.domain.user.User;
import org.example.what_seoul.exception.UnauthorizedException;
import org.example.what_seoul.repository.user.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public CommonResponse<ResReissueAccessTokenDTO> reissueAccessToken(String refreshToken, HttpServletResponse response) {
        // 1. refreshToken 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.info("authService - reissueAccessToken");
            throw new UnauthorizedException("유효하지 않거나 만료된 토큰입니다.");
        }

        // 2. refreshToken에서 클레임 추출
        Claims claims = jwtTokenProvider.getClaimsFromToken(refreshToken);
        String tokenType = claims.get("type", String.class);
        if (!tokenType.equals("refresh")) {
            throw new UnauthorizedException("리프레시 토큰이 아닙니다.");
        }

        // 3. 사용자 존재 여부 확인
        String userId = claims.getSubject();
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다."));

        // 4. accessToken 갱신
        String role = claims.get("role", String.class);
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), role);

        // 5. accessToken을 새로 쿠키에 저장
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        return new CommonResponse<>(true, "액세스 토큰 재발급 성공", new ResReissueAccessTokenDTO(jwtTokenProvider.getAccessTokenExpirationTime(accessToken)));
    }

    @Transactional
    public CommonResponse<Void> logout(String accessToken, HttpServletResponse response) {
        // Authorization Header로 넘긴다면 접두어를 반영한 substring 필요

        // 1. accessToken 유효성 검증
        if (!jwtTokenProvider.validateToken(accessToken)) {
            log.info("adminService - logout");
            throw new UnauthorizedException("유효하지 않거나 만료된 토큰입니다.");
        }

        // 2. accessToken에서 클레임 추출 및 타입 검증
        Claims claims = jwtTokenProvider.getClaimsFromToken(accessToken);
        String tokenType = claims.get("type", String.class);
        if (!tokenType.equals("access")) {
            throw new UnauthorizedException("액세스 토큰이 아닙니다.");
        }

        // 3. 사용자 존재 여부 확인
        String userId = claims.getSubject();
        userRepository.findByUserId(userId).orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다."));

        // 4. redis에 저장된 refreshToken을 삭제
        redisTemplate.delete("RT:" + userId);

        // 5. 쿠키에 담긴 accessToken, refreshToken을 즉시 만료시킴
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true) // HTTPS 환경에서만 전송
                .path("/")
                .maxAge(0) // 수명 0초로 즉시 만료
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true) // HTTPS 환경에서만 전송
                .path("/")
                .maxAge(0) // 수명 0초로 즉시 만료
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return new CommonResponse<>(true, "로그아웃 성공", null);
    }
}
