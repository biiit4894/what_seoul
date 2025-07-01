package org.example.what_seoul.service.admin;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.common.validation.CustomValidator;
import org.example.what_seoul.config.JwtTokenProvider;
import org.example.what_seoul.controller.admin.dto.ReqAdminLoginDTO;
import org.example.what_seoul.controller.admin.dto.ReqCreateAdminDTO;
import org.example.what_seoul.controller.admin.dto.ResAdminLoginDTO;
import org.example.what_seoul.controller.admin.dto.ResCreateAdminDTO;
import org.example.what_seoul.domain.user.RoleType;
import org.example.what_seoul.domain.user.User;
import org.example.what_seoul.exception.CustomValidationException;
import org.example.what_seoul.exception.UnauthorizedException;
import org.example.what_seoul.repository.user.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
    private final CustomValidator customValidator;
    private final RedisTemplate<String, String> redisTemplate;


    /**
     * 관리자 계정 생성 기능
     * - request DTO 유효성 검증과 중복 값 검증을 함께 진행한다.
     * - 이를 위해 @Valid 애노테이션을 사용하는 대신, ValidatorFactory를 수동으로 생성한다.
     * @param req 회원 가입에 필요한 요청 데이터 DTO
     * @return 관리자 계정 생성 성공 시 CommonResponse를, 실패 시 CommonErrorResponse를 반환한다.
     */
    @Transactional
    public CommonResponse<ResCreateAdminDTO> createAdminUser(String accessToken, ReqCreateAdminDTO req) {
        // 1. 토큰 유효성 검사
        if (!jwtTokenProvider.validateToken(accessToken)) {
            log.info("adminService - createAdminUser");
            throw new UnauthorizedException("유효하지 않거나 만료된 관리자 토큰입니다.");
        }

        // 2. role 확인
        Claims claims = jwtTokenProvider.getClaimsFromToken(accessToken);
        String role = claims.get("role", String.class);
        if (!role.equals("ADMIN")) {
            throw new AccessDeniedException("관리자 권한이 없습니다.");
        }

        Map<String, List<String>> errors = new HashMap<>();

        // 1. Request DTO 유효성 검증
        Set<ConstraintViolation<ReqCreateAdminDTO>> violations = customValidator.validate(req);

        for (ConstraintViolation<ReqCreateAdminDTO> violation : violations) {
            errors.computeIfAbsent(violation.getPropertyPath().toString(), key -> new ArrayList<>())
                    .add(violation.getMessage());
        }

        // 2. 중복 값 검증
        if (userRepository.findByUserId(req.getUserId()).isPresent()) {
            if (errors.containsKey("userId")) {
                errors.get("userId").add("이미 사용 중인 아이디입니다.");
            } else {
                errors.computeIfAbsent("userId", key -> new ArrayList<>()).add("이미 사용 중인 아이디입니다.");
            }
        }

        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            if (errors.containsKey("email")) {
                errors.get("email").add("이미 사용 중인 이메일입니다.");
            } else {
                errors.computeIfAbsent("email", key -> new ArrayList<>()).add("이미 사용 중인 이메일입니다.");
            }
        }

        if (userRepository.findByNickName(req.getNickName()).isPresent()) {
            if (errors.containsKey("nickName")) {
                errors.get("nickName").add("이미 사용 중인 닉네임입니다.");
            } else {
                errors.computeIfAbsent("nickName", key -> new ArrayList<>()).add("이미 사용 중인 닉네임입니다.");
            }
        }

        // 3. 1)유효성 검증 및 2)중복 검증에서 발생한 모든 에러를 포함하여 예외를 던진다.
        if (!errors.isEmpty()) {
            throw new CustomValidationException(errors);
        }

        User newUser = new User(
                req.getUserId(),
                encoder.encode(req.getPassword()),
                req.getEmail(),
                req.getNickName(),
                RoleType.ADMIN
        );

        userRepository.save(newUser);

        return new CommonResponse<>(
                true,
                "관리자 계정 생성 성공",
                ResCreateAdminDTO.from(newUser)
        );
    }

    /**
     * 관리자 계정 로그인 기능
     * @param req
     * @return
     */
    @Transactional
    public CommonResponse<ResAdminLoginDTO> login(ReqAdminLoginDTO req, HttpServletResponse response) {
        User admin = userRepository.findByUserId(req.getUserId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        if (!admin.getRole().equals(RoleType.ADMIN)) {
            throw new IllegalArgumentException("관리자 계정이 아닙니다.");
        }

        if (!encoder.matches(req.getPassword(), admin.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(admin.getUserId(), RoleType.ADMIN.name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(admin.getUserId(), RoleType.ADMIN.name());

        redisTemplate.opsForValue().set(
                "RT:" + admin.getUserId(),
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpirationMs(),
                TimeUnit.MILLISECONDS
        );

        // 3. AccessToken 쿠키로 전달
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(true) // HTTPS 환경에서만 전송
                .path("/")
                .maxAge(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .sameSite("Strict")
                .build();

        // 4. RefreshToken 쿠키로 전달
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(jwtTokenProvider.getRefreshTokenExpirationMs() / 1000)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return new CommonResponse<>(
                true,
                "관리자 로그인 성공",
                new ResAdminLoginDTO(admin.getUserId(), jwtTokenProvider.getAccessTokenExpirationTime(accessToken))
        );
    }


}
