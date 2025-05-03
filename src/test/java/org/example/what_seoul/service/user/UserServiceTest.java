package org.example.what_seoul.service.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.common.validation.CustomValidator;
import org.example.what_seoul.controller.user.dto.*;
import org.example.what_seoul.domain.user.User;
import org.example.what_seoul.exception.CustomValidationException;
import org.example.what_seoul.repository.user.UserRepository;
import org.hibernate.validator.internal.engine.path.PathImpl;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
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
    @DisplayName("[성공] 회원가입 Service")
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
    @DisplayName("[성공] 회원정보 리스트 조회 Service")
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
    @DisplayName("[성공] 회원정보 상세 조회 Service")
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

    @DisplayName("[성공] 회원 정보 수정 Service")
    @Test
    void updateUserInfo() throws JsonProcessingException {
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

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(json);
    }

    @DisplayName("[성공] 회원 탈퇴 Service")
    @Test
    void withdrawUser() throws JsonProcessingException {
        // Given
        Long id = 1L;
        User user = new User("testUser", encoder.encode("password"), "user@example.com", "nickname");
        ReflectionTestUtils.setField(user, "id", id); // 테스트 환경에서 id 값 설정하기

        // SecurityContextHolder에 인증 정보 세팅
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        // When
        CommonResponse<ResWithdrawUserDTO> response = userService.withdrawUser(httpServletRequest, httpServletResponse);

        // Then
        assertTrue(response.isSuccess());
        assertEquals("회원 탈퇴 성공", response.getMessage());
        assertNotNull(user.getDeletedAt());

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(json);
    }

    @DisplayName("[실패] 회원가입 Service - 중복 값 존재")
    @Test
    void createUser_DuplicateFields() {
        // Given
        ReqCreateUserDTO req = new ReqCreateUserDTO("duplicateUser", "test1234!", "duplicate@test.com", "duplicateNick");

        when(customValidator.validate(req)).thenReturn(Collections.emptySet());

        when(userRepository.findByUserId("duplicateUser")).thenReturn(Optional.of(mock(User.class)));
        when(userRepository.findByEmail("duplicate@test.com")).thenReturn(Optional.of(mock(User.class)));
        when(userRepository.findByNickName("duplicateNick")).thenReturn(Optional.of(mock(User.class)));

        // When / Then
        CustomValidationException ex = assertThrows(CustomValidationException.class, () -> userService.createUser(req));

        Map<String, List<String>> errors = ex.getErrors();
        assertEquals(3, errors.size());
        assertTrue(errors.get("userId").contains("이미 사용 중인 아이디입니다."));
        assertTrue(errors.get("email").contains("이미 사용 중인 이메일입니다."));
        assertTrue(errors.get("nickName").contains("이미 사용 중인 닉네임입니다."));
    }


    @DisplayName("[실패] 회원가입 Service - 유효성 검사 실패")
    @Test
    void createUser_InvalidFields() {
        // Given
        ReqCreateUserDTO req = new ReqCreateUserDTO("user", "123", "invalidEmail", "nick");

        ConstraintViolation<ReqCreateUserDTO> violation1 = mock(ConstraintViolation.class);
        when(violation1.getPropertyPath()).thenReturn(PathImpl.createPathFromString("password"));
        when(violation1.getMessage()).thenReturn("비밀번호는 최소 8자 이상이어야 합니다.");

        ConstraintViolation<ReqCreateUserDTO> violation2 = mock(ConstraintViolation.class);
        when(violation2.getPropertyPath()).thenReturn(PathImpl.createPathFromString("email"));
        when(violation2.getMessage()).thenReturn("이메일 형식이 올바르지 않습니다.");

        Set<ConstraintViolation<ReqCreateUserDTO>> violations = Set.of(violation1, violation2);

        when(customValidator.validate(req)).thenReturn(violations);

        // When / Then
        CustomValidationException ex = assertThrows(CustomValidationException.class, () -> userService.createUser(req));

        Map<String, List<String>> errors = ex.getErrors();
        assertEquals(2, errors.size());
        assertTrue(errors.get("password").contains("비밀번호는 최소 8자 이상이어야 합니다."));
        assertTrue(errors.get("email").contains("이메일 형식이 올바르지 않습니다."));
    }

    @DisplayName("[실패] 회원 리스트 조회 Service - 잘못된 페이지 번호")
    @Test
    void getUserList_InvalidPageNumber() {
        // Given
        int invalidPage = -1;
        int size = 10;

        // Expect
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserList(invalidPage, size);
        });

        assertEquals("Page index must not be less than zero", exception.getMessage());
    }

    @DisplayName("[실패] 회원 리스트 조회 Service - 잘못된 페이지 크기")
    @Test
    void getUserList_InvalidPageSize() {
        // Given
        int page = 0;
        int invalidSize = -5;

        // Expect
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserList(page, invalidSize);
        });

        assertEquals("Page size must not be less than one", exception.getMessage());
    }


    @DisplayName("[실패] 회원 정보 상세 조회 Service - 사용자를 찾을 수 없음")
    @Test
    void getUserDetailById_UserNotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserDetailById(userId);
        });

        assertEquals("사용자를 찾을 수 없습니다.", exception.getMessage());
    }

    @DisplayName("[실패] 회원 정보 수정 Service - 변경 항목 없음")
    @Test
    void updateUserInfo_NothingToUpdate() {
        // Given
        Long userId = 1L;
        User user = new User("testUser", encoder.encode("currPassword"), "user@example.com", "nick");
        ReflectionTestUtils.setField(user, "id", userId);

        // SecurityContextHolder에 인증 정보 세팅
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        ReqUpdateUserInfoDTO req = new ReqUpdateUserInfoDTO("currPassword", null, null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(encoder.matches("currPassword", user.getPassword())).thenReturn(true);

        // Expect
        CustomValidationException exception = assertThrows(CustomValidationException.class, () -> {
            userService.updateUserInfo(req);
        });

        assertTrue(exception.getErrors().containsKey("nothingToUpdate"));
    }

    @DisplayName("[실패] 회원 탈퇴 Service - 이미 탈퇴한 사용자")
    @Test
    void withdrawUser_AlreadyWithdrawn() {
        // Given
        Long userId = 1L;
        User user = new User("testUser", encoder.encode("currPassword"), "user@example.com", "nick");
        ReflectionTestUtils.setField(user, "id", userId);
        user.deactivate();  // 이미 탈퇴 처리된 상태

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // SecurityContext에 인증 정보 세팅
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // Expect
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            userService.withdrawUser(httpServletRequest, httpServletResponse);
        });

        assertEquals("이미 탈퇴한 사용자입니다.", exception.getMessage());
    }

    @DisplayName("[실패] 회원 탈퇴 Service - 로그인한 사용자 정보를 찾을 수 없음")
    @Test
    void withdrawUser_LoginUserInfoNotFound() {
        // SecurityContext에 인증 정보 세팅
        Authentication auth = new UsernamePasswordAuthenticationToken(null, null, new ArrayList<>());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // Expect
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.withdrawUser(httpServletRequest, httpServletResponse);
        });

        assertEquals("로그인한 사용자 정보가 없습니다.", exception.getMessage());
    }
}
