package org.example.what_seoul.service.auth;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.config.JwtTokenProvider;
import org.example.what_seoul.controller.auth.dto.ResReissueAccessTokenDTO;
import org.example.what_seoul.domain.user.User;
import org.example.what_seoul.exception.UnauthorizedException;
import org.example.what_seoul.repository.user.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public CommonResponse<ResReissueAccessTokenDTO> reissueAccessToken(String refreshToken) {
        String resolvedToken = refreshToken.substring(7);

        // 1. refreshToken 유효성 검증
        if (!jwtTokenProvider.validateToken(resolvedToken)) {
            throw new UnauthorizedException("유효하지 않거나 만료된 토큰입니다.");
        }

        // 2. refreshToken에서 클레임 추출
        Claims claims = jwtTokenProvider.getClaimsFromToken(resolvedToken);
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

        return new CommonResponse<>(true, "액세스 토큰 재발급 성공", new ResReissueAccessTokenDTO(accessToken));
    }
}
