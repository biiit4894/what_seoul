package org.example.what_seoul.service.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.validation.ConstraintViolation;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.common.validation.CustomValidator;
import org.example.what_seoul.config.JwtTokenProvider;
import org.example.what_seoul.controller.admin.dto.ReqCreateAdminDTO;
import org.example.what_seoul.controller.admin.dto.ResCreateAdminDTO;
import org.example.what_seoul.domain.user.User;
import org.example.what_seoul.exception.CustomValidationException;
import org.example.what_seoul.exception.UnauthorizedException;
import org.example.what_seoul.repository.user.UserRepository;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class AdminServiceTest {
    @InjectMocks
    private AdminService adminService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder encoder;

    @Mock
    private CustomValidator customValidator;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AdminServiceTest() {
        objectMapper.registerModule(new JavaTimeModule());
    }


    @Test
    @DisplayName("[성공] 관리자 계정 생성 Service")
    void signup() throws JsonProcessingException {
        // Given
        String accessToken = "accessToken";
        ReqCreateAdminDTO req = new ReqCreateAdminDTO("admin", "password", "test", "test");

        Claims claims = Jwts.claims().setSubject("admin");
        claims.put("role", "ADMIN");

        given(jwtTokenProvider.validateToken(accessToken)).willReturn(true);
        given(jwtTokenProvider.getClaimsFromToken(accessToken)).willReturn(claims);
        given(customValidator.validate(req)).willReturn(Collections.emptySet());

        given(userRepository.findByUserId("admin")).willReturn(Optional.empty());
        given(userRepository.findByEmail("test")).willReturn(Optional.empty());
        given(userRepository.findByNickName("test")).willReturn(Optional.empty());
        given(encoder.encode("password")).willReturn("encodedPassword");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        given(userRepository.save(userCaptor.capture())).willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return user;
        });

        // When
        CommonResponse<ResCreateAdminDTO> response = adminService.createAdminUser(accessToken, req);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("관리자 계정 생성 성공");
        assertThat(response.getData().getUserId()).isEqualTo("admin");
        assertThat(response.getData().getNickName()).isEqualTo("test");
        assertThat(response.getData().getEmail()).isEqualTo("test");
        assertThat(response.getData().getCreatedAt()).isNotNull();

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(json);
    }

    @Test
    @DisplayName("[실패] 관리자 계정 생성 Service - 토큰 유효성 검증 실패")
    void createAdminUser_invalidToken() {
        // Given
        String accessToken = "accessToken";
        ReqCreateAdminDTO req = new ReqCreateAdminDTO("admin", "password", "test", "test");

        given(jwtTokenProvider.validateToken(accessToken)).willReturn(false);

        // When & Then
        assertThrows(UnauthorizedException.class, () -> adminService.createAdminUser(accessToken, req));
    }

    @Test
    @DisplayName("[실패] 관리자 계정 생성 Service - 관리자 권한이 아닌 경우")
    void createAdminUser_notAdminUser() {
        // Given
        String accessToken = "accessToken";
        ReqCreateAdminDTO req = new ReqCreateAdminDTO("admin", "password", "test", "test");

        Claims claims = Jwts.claims().setSubject("user");
        claims.put("role", "USER");

        given(jwtTokenProvider.validateToken(accessToken)).willReturn(true);
        given(jwtTokenProvider.getClaimsFromToken(accessToken)).willReturn(claims);

        // When & Then
        assertThrows(AccessDeniedException.class, () -> adminService.createAdminUser(accessToken, req));
    }

    @Test
    @DisplayName("[실패] 관리자 계정 생성 Service - 필수 입력값을 누락한 경우")
    void createAdminUser_requestBodyValidationFailed() {
        // Given
        String accessToken = "accessToken";
        ReqCreateAdminDTO req = new ReqCreateAdminDTO("", "abc@1234", "test@mail.com", "nickname");

        Claims claims = Jwts.claims().setSubject("admin");
        claims.put("role", "ADMIN");

        given(jwtTokenProvider.validateToken(accessToken)).willReturn(true);
        given(jwtTokenProvider.getClaimsFromToken(accessToken)).willReturn(claims);

        ConstraintViolation<ReqCreateAdminDTO> violation = mock(ConstraintViolation.class);
        given(violation.getPropertyPath()).willReturn(PathImpl.createPathFromString("userId"));
        given(violation.getMessage()).willReturn("아이디는 필수 입력값입니다.");

        Set<ConstraintViolation<ReqCreateAdminDTO>> violations = new HashSet<>();
        violations.add(violation);

        given(customValidator.validate(req)).willReturn(violations);

        // When & Then
        CustomValidationException exception = assertThrows(CustomValidationException.class, () -> adminService.createAdminUser(accessToken, req));

        assertTrue(exception.getErrors().containsKey("userId"));
        assertTrue(exception.getErrors().get("userId").contains("아이디는 필수 입력값입니다."));
    }

    @Test
    @DisplayName("[실패] 관리자 계정 생성 Service - 비밀번호 길이 제한을 위반한 경우")
    void createAdminUser_sizeViolation() {
        // Given
        String accessToken = "accessToken";
        ReqCreateAdminDTO req = new ReqCreateAdminDTO("admin", "t", "test@mail.com", "nickname");

        Claims claims = Jwts.claims().setSubject("admin");
        claims.put("role", "ADMIN");

        given(jwtTokenProvider.validateToken(accessToken)).willReturn(true);
        given(jwtTokenProvider.getClaimsFromToken(accessToken)).willReturn(claims);

        ConstraintViolation<ReqCreateAdminDTO> violation = mock(ConstraintViolation.class);
        given(violation.getPropertyPath()).willReturn(PathImpl.createPathFromString("password"));
        given(violation.getMessage()).willReturn("비밀번호는 4자 이상 20자 이하로 입력해야 합니다.");

        Set<ConstraintViolation<ReqCreateAdminDTO>> violations = Set.of(violation);
        given(customValidator.validate(req)).willReturn(violations);

        // When & Then
        CustomValidationException exception = assertThrows(CustomValidationException.class, () -> adminService.createAdminUser(accessToken, req));

        assertTrue(exception.getErrors().get("password").contains("비밀번호는 4자 이상 20자 이하로 입력해야 합니다."));
    }

    @Test
    @DisplayName("[실패] 관리자 계정 생성 Service - 올바르지 않은 형식의 이메일을 입력한 경우")
    void createAdminUser_patternViolation() {
        // Given
        String token = "validToken";
        ReqCreateAdminDTO req = new ReqCreateAdminDTO("admin", "abc@1234", "test@mail.com", "nickname");

        Claims claims = Jwts.claims().setSubject("admin");
        claims.put("role", "ADMIN");

        given(jwtTokenProvider.validateToken(token)).willReturn(true);
        given(jwtTokenProvider.getClaimsFromToken(token)).willReturn(claims);

        ConstraintViolation<ReqCreateAdminDTO> violation = mock(ConstraintViolation.class);
        given(violation.getPropertyPath()).willReturn(PathImpl.createPathFromString("email"));
        given(violation.getMessage()).willReturn("올바른 이메일 형식이 아닙니다.");

        given(customValidator.validate(req)).willReturn(Set.of(violation));

        // When & Then
        CustomValidationException exception =
                assertThrows(CustomValidationException.class, () -> adminService.createAdminUser(token, req));

        assertTrue(exception.getErrors().get("email")
                .contains("올바른 이메일 형식이 아닙니다."));
    }

    @Test
    @DisplayName("[실패] 관리자 계정 생성 Service - 이미 사용 중인 아이디를 입력한 경우")
    void createAdminUser_userIdExists() {
        // Given
        String accessToken = "accessToken";
        ReqCreateAdminDTO req = new ReqCreateAdminDTO("admin", "abc@123", "test@mail.com", "admin");

        Claims claims = Jwts.claims().setSubject("admin");
        claims.put("role", "ADMIN");

        given(jwtTokenProvider.validateToken(accessToken)).willReturn(true);
        given(jwtTokenProvider.getClaimsFromToken(accessToken)).willReturn(claims);
        given(customValidator.validate(req)).willReturn(Collections.emptySet());

        given(userRepository.findByUserId("admin")).willReturn(Optional.of(new User()));

        // When & Then
        CustomValidationException exception =
                assertThrows(CustomValidationException.class, () -> adminService.createAdminUser(accessToken, req));

        List<String> messages = exception.getErrors().get("userId");
        assertTrue(messages.contains("이미 사용 중인 아이디입니다."));
    }

    @Test
    @DisplayName("[실패] 관리자 계정 생성 Service - 이미 사용 중인 이메일을 입력한 경우")
    void createAdminUser_emailExists() {
        // Given
        String accessToken = "accessToken";
        ReqCreateAdminDTO req = new ReqCreateAdminDTO("admin", "abc@123", "test@mail.com", "admin");

        Claims claims = Jwts.claims().setSubject("admin");
        claims.put("role", "ADMIN");

        given(jwtTokenProvider.validateToken(accessToken)).willReturn(true);
        given(jwtTokenProvider.getClaimsFromToken(accessToken)).willReturn(claims);
        given(customValidator.validate(req)).willReturn(Collections.emptySet());

        given(userRepository.findByEmail("test@mail.com")).willReturn(Optional.of(new User()));

        // When & Then
        CustomValidationException exception =
                assertThrows(CustomValidationException.class, () -> adminService.createAdminUser(accessToken, req));

        List<String> messages = exception.getErrors().get("email");
        assertTrue(messages.contains("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("[실패] 관리자 계정 생성 Service - 이미 사용 중인 닉네임을 입력한 경우")
    void createAdminUser_nickNameExists() {
        // Given
        String accessToken = "accessToken";
        ReqCreateAdminDTO req = new ReqCreateAdminDTO("admin", "abc@123", "test@mail.com", "admin");

        Claims claims = Jwts.claims().setSubject("admin");
        claims.put("role", "ADMIN");

        given(jwtTokenProvider.validateToken(accessToken)).willReturn(true);
        given(jwtTokenProvider.getClaimsFromToken(accessToken)).willReturn(claims);
        given(customValidator.validate(req)).willReturn(Collections.emptySet());

        given(userRepository.findByNickName("admin")).willReturn(Optional.of(new User()));

        // When & Then
        CustomValidationException exception =
                assertThrows(CustomValidationException.class, () -> adminService.createAdminUser(accessToken, req));

        List<String> messages = exception.getErrors().get("nickName");
        assertTrue(messages.contains("이미 사용 중인 닉네임입니다."));
    }
}
