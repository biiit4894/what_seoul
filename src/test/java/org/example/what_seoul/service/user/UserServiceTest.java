package org.example.what_seoul.service.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.common.validation.CustomValidator;
import org.example.what_seoul.controller.user.dto.*;
import org.example.what_seoul.domain.user.User;
import org.example.what_seoul.repository.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder encoder;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private UserDetailService userDetailService;

    @Mock
    private CustomValidator customValidator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserServiceTest() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("회원가입 서비스 성공 테스트")
    void createUser() throws JsonProcessingException {
        // Given
        ReqCreateUserDTO req = new ReqCreateUserDTO("test", "test1234!", "test@test.com", "testNickName");

        when(customValidator.validate(req)).thenReturn(Collections.emptySet());

        when(userRepository.findByUserId("test")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByNickName("testNickName")).thenReturn(Optional.empty());

        when(encoder.encode("test1234!")).thenReturn("encodedPassword");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        User savedUser = new User("test", "test1234!", "test@test.com", "testNickName");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        CommonResponse<ResCreateUserDTO> response = userService.createUser(req);

        // Then
        verify(userRepository).save(userCaptor.capture()); // save() 호출 시 전달된 인자(User 객체)를 캡처
        User capturedUser = userCaptor.getValue();

        assertTrue(response.isSuccess());
        assertEquals("회원 가입 성공", response.getMessage());
        assertEquals("test", response.getData().getUserId());
        assertEquals("test@test.com", response.getData().getEmail());
        assertEquals("testNickName", response.getData().getNickName());
        assertNotNull(response.getData().getCreatedAt());

        assertEquals("test", capturedUser.getUserId());
        assertEquals("encodedPassword", capturedUser.getPassword());

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(json);

    }

    @Test
    @DisplayName("회원정보 리스트 조회 서비스 성공 테스트")
    void getUserList() throws JsonProcessingException {
        // Given
        int page = 0;
        int size = 5;

        // Mocking pageable and user repository
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<User> userList = List.of(
                new User("user1", "password1", "user1@test.com", "nick1"),
                new User("user2", "password2", "user2@test.com", "nick2")
        );

        Page<User> usersPage = new PageImpl<>(userList, pageable, userList.size());
        when(userRepository.findAll(pageable)).thenReturn(usersPage);

        // When
        CommonResponse<Page<ResGetUserDetailSummaryDTO>> response = userService.getUserList(page, size);

        // Then
        assertTrue(response.isSuccess());
        assertEquals("회원 정보 리스트 조회 성공", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(userList.size(), response.getData().getTotalElements());

        // Verify the mapping of users to DTOs
        List<ResGetUserDetailSummaryDTO> userSummaryList = response.getData().getContent();
        assertEquals(userList.size(), userSummaryList.size());
        assertEquals("user1", userSummaryList.get(0).getUserId());
        assertEquals("user2", userSummaryList.get(1).getUserId());

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(json);
    }

    @Test
    @DisplayName("회원정보 상세 조회 서비스 성공 테스트")
    void getUserDetailById() throws JsonProcessingException {
        // Given
        Long id = 1L;
        User user = new User("test", "encodedPassword", "test@example.com", "testNickName");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        // When
        CommonResponse<ResGetUserDetailDTO> response = userService.getUserDetailById(id);

        // Then
        assertTrue(response.isSuccess());
        assertEquals("회원 정보 상세 조회 성공", response.getMessage());
        assertEquals("test", response.getData().getUserId());
        assertEquals("test@example.com", response.getData().getEmail());
        assertEquals("testNickName", response.getData().getNickName());

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(json);
    }

    @DisplayName("회원 정보 수정 서비스 성공 테스트")
    @Test
    void updateUserInfo() {
        // Given
        Long id = 1L;
        User user = new User("testUser", encoder.encode("currPassword"), "user@example.com", "nick");
        ReflectionTestUtils.setField(user, "id", id); // 테스트 환경에서 id 값 설정하기

        // SecurityContextHolder에 인증 정보 세팅
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        ReqUpdateUserInfoDTO req = new ReqUpdateUserInfoDTO("currPassword", "newPassword", "new@example.com", "newNick");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByNickName("newNick")).thenReturn(false);
        when(encoder.matches("currPassword", user.getPassword())).thenReturn(true);
        when(encoder.encode("newPassword")).thenReturn("encodedNewPassword");

        // When
        CommonResponse<ResUpdateUserDTO> response = userService.updateUserInfo(req);

        // Then
        assertTrue(response.isSuccess());
        assertEquals("회원 정보 수정 성공", response.getMessage());
        assertEquals("new@example.com", response.getData().getEmail());
        assertEquals("newNick", response.getData().getNickName());
    }

    @DisplayName("회원 탈퇴 서비스 성공 테스트")
    @Test
    void withdrawUser() {
        // Given
        Long id = 1L;
        User user = new User("testUser", encoder.encode("password"), "user@example.com", "nickname");
        ReflectionTestUtils.setField(user, "id", id); // 테스트 환경에서 id 값 설정하기

        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);


        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        // When
        CommonResponse<ResWithdrawUserDTO> result = userService.withdrawUser(httpServletRequest, httpServletResponse);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("회원 탈퇴 성공", result.getMessage());
        assertNotNull(user.getDeletedAt());
    }



}
