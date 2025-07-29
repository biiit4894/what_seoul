package org.example.what_seoul.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.what_seoul.controller.user.dto.ReqUserLoginDTO;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-h2")
@Transactional
public class UserIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Test
    @DisplayName("[성공] 일반 회원 로그인 통합 테스트 - 쿠키 발급 및 Redis 저장 확인")
    void login() throws Exception {
        // given
        String userId = "test";
        String rawPassword = "password";
        String encodedPassword = encoder.encode(rawPassword);

        userRepository.save(new User(userId, encodedPassword, "test", "test", RoleType.USER));

        ReqUserLoginDTO loginRequest = new ReqUserLoginDTO(userId, rawPassword);

        MvcResult result = mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andReturn();

        // then - Redis 저장 여부
        String redisKey = "RT:" + userId;
        String refreshToken = redisTemplate.opsForValue().get(redisKey);
        assertThat(refreshToken).isNotNull();
    }
}
