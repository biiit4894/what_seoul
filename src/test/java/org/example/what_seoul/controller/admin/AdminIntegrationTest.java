package org.example.what_seoul.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.example.what_seoul.config.JwtTokenProvider;
import org.example.what_seoul.controller.admin.dto.ReqCreateAdminDTO;
import org.example.what_seoul.domain.user.RoleType;
import org.example.what_seoul.domain.user.User;
import org.example.what_seoul.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-h2")
@Transactional
public class AdminIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("[성공] 관리자 계정 생성 통합 테스트")
    void createAdminUser_success() throws Exception {
        // Given
        // 사전에 유효한 관리자 토큰 생성
        String adminUserId = "admin";
        String rawPassword = "password";
        User admin = new User(adminUserId, encoder.encode(rawPassword), "admin@mail.com", "관리자", RoleType.ADMIN);
        userRepository.save(admin);

        String accessToken = jwtTokenProvider.generateAccessToken(admin.getUserId(), RoleType.ADMIN.name());

        ReqCreateAdminDTO req = new ReqCreateAdminDTO("admin2", "abc@123", "admin2@mail.com", "관리자2");

        // When & Then
        mockMvc.perform(post("/api/admin/signup")
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("admin2"))
                .andExpect(jsonPath("$.data.email").value("admin2@mail.com"))
                .andExpect(jsonPath("$.data.nickName").value("관리자2"))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    @DisplayName("[실패] 관리자 계정 생성 통합 테스트 - accessToken 누락") // 인증 관련 테스트(토큰 없이 요청)
    void createAdminUser_withoutToken() throws Exception {
        ReqCreateAdminDTO req = new ReqCreateAdminDTO("admin2", "abc@123", "admin2@mail.com", "관리자2");

        mockMvc.perform(post("/api/admin/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized()) // 인증 객체가 없어 JwtAuthenticationEntryPoint에서 예외처리
                .andExpect(jsonPath("$.message").value("Unauthorized"))
                .andExpect(jsonPath("$.context").value("유효하지 않거나 만료된 토큰입니다."));
    }

    @Test
    @DisplayName("[실패] 관리자 계정 생성 통합 테스트 - 유효하지 않은 accessToken") // 인증 관련 테스트(유효하지 않은 토큰 - 서명 오류)
    void createAdminUser_withInvalidToken() throws Exception {
        // Given
        String invalidToken = "invalidJwtToken";
        ReqCreateAdminDTO req = new ReqCreateAdminDTO("admin", "abc@123", "admin@mail.com", "관리자");

        mockMvc.perform(post("/api/admin/signup")
                        .cookie(new Cookie("accessToken", invalidToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized"))
                .andExpect(jsonPath("$.context").value("유효하지 않거나 만료된 토큰입니다."));
    }

    @Test
    @DisplayName("[실패] 관리자 계정 생성 통합 테스트 - 만료된 accessToken") // 인증 관련 테스트 (만료된 토큰)
    void createAdminUser_withExpiredToken() throws Exception {
        // Given
        String expiredToken = "expiredJwtToken";
        ReqCreateAdminDTO req = new ReqCreateAdminDTO("admin2", "abc@123", "admin2@mail.com", "관리자2");

        mockMvc.perform(post("/api/admin/signup")
                        .cookie(new Cookie("accessToken", expiredToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized"))
                .andExpect(jsonPath("$.context").value("유효하지 않거나 만료된 토큰입니다."));
    }

    @Test
    @DisplayName("[실패] 관리자 계정 생성 통합 테스트 - 일반 유저의 토큰 전달") // 인가 관련 테스트(유효하지만, 권한이 다른 토큰)
    void createAdminUser_withUserRole() throws Exception {
        // Given
        // 사전에 일반 유저 토큰 생성
        User user = new User("user", encoder.encode("password1!"), "user@mail.com", "일반 사용자", RoleType.USER);
        userRepository.save(user);
        String userToken = jwtTokenProvider.generateAccessToken(user.getUserId(), RoleType.USER.name());

        ReqCreateAdminDTO req = new ReqCreateAdminDTO("admin2", "abc@123", "admin02@mail.com", "관리자2");

        mockMvc.perform(post("/api/admin/signup")
                        .cookie(new Cookie("accessToken", userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Forbidden"))
                .andExpect(jsonPath("$.context").value("접근 권한이 없습니다."));
    }
}
