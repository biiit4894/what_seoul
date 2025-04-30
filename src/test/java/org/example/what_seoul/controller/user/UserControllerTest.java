package org.example.what_seoul.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.config.WebSecurityTestConfig;
import org.example.what_seoul.controller.user.dto.*;
import org.example.what_seoul.domain.user.User;
import org.example.what_seoul.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(WebSecurityTestConfig.class)  // 테스트를 위한 custom security configuration
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("[성공] 회원가입 Controller")
    void signup() throws Exception {
        // Given
        ReqCreateUserDTO req = new ReqCreateUserDTO("test", "password123!", "test@example.com", "test" );
        User user = new User("test", "password123!", "test@example.com", "test");
        ResCreateUserDTO res = ResCreateUserDTO.from(user);
        CommonResponse<ResCreateUserDTO> commonResponse = new CommonResponse<>(true, "회원 가입 성공", res);

        // When & Then
        when(userService.createUser(any(ReqCreateUserDTO.class))).thenReturn(commonResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("test"));
    }

    @Test
    @WithMockUser(username = "test", roles = "ADMIN")
    @DisplayName("[성공] 회원정보 리스트 조회 Controller")
    void getUserList() throws Exception {
        // Given - 회원 목록 Mock 데이터 생성
        User user1 = new User("test1", "test1234!", "test1@test1.com", "nickname1");
        User user2 = new User("test2", "test1234!", "test2@test2.com", "nickname2");

        List<ResGetUserDetailSummaryDTO> userList = List.of(
                ResGetUserDetailSummaryDTO.from(user1),
                ResGetUserDetailSummaryDTO.from(user2)
        );

        Page<ResGetUserDetailSummaryDTO> pageResult = new PageImpl<>(userList, PageRequest.of(0, 10), userList.size());
        CommonResponse<Page<ResGetUserDetailSummaryDTO>> commonResponse = new CommonResponse<>(true, "회원 정보 리스트 조회 성공", pageResult);

        // When & Then
        when(userService.getUserList(anyInt(), anyInt())).thenReturn(commonResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/list")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].userId").value("test1"))
                .andExpect(jsonPath("$.data.content[1].userId").value("test2"));
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[성공] 회원정보 상세 조회 Controller")
    void getUserDetailById() throws Exception {
        // Given - SecurityContext에 로그인 사용자 설정
        String username = "test";

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password("encodedPassword") // 굳이 실제 비밀번호가 아니어도 됨
                .roles("USER")  // 권한 설정
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Given - 조회할 유저 Mock 데이터 설정
        Long id = 1L;
        User user = new User("test", "test1234!", "test@test.com", "testNickName");
        ResGetUserDetailDTO res = ResGetUserDetailDTO.from(user);
        CommonResponse<ResGetUserDetailDTO> commonResponse = new CommonResponse<>(true, "회원 정보 상세 조회 성공", res);

        // When & Then
        when(userService.getUserDetailById(id)).thenReturn(commonResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/{id}", id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("test"))
                .andExpect(jsonPath("$.data.email").value("test@test.com"))
                .andExpect(jsonPath("$.data.nickName").value("testNickName"));
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[성공] 회원정보 수정 Controller")
    void updateUserInfo() throws Exception {
        // Given - 요청 DTO 준비
        ReqUpdateUserInfoDTO req = new ReqUpdateUserInfoDTO("password123!", "newPassword123!", "newemail@test.com", "newNickname");

        // Given - 응답 DTO 준비
        User updatedUser = new User("test", "newPassword123!", "newemail@test.com", "newNickname");
        ResUpdateUserDTO res = ResUpdateUserDTO.from(updatedUser);
        CommonResponse<ResUpdateUserDTO> commonResponse = new CommonResponse<>(true, "회원 정보 수정 성공", res);

        // When & Then
        when(userService.updateUserInfo(any(ReqUpdateUserInfoDTO.class))).thenReturn(commonResponse);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("newemail@test.com"))
                .andExpect(jsonPath("$.data.nickName").value("newNickname"));
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[성공] 회원탈퇴 Controller")
    void withdrawUser() throws Exception {
        // Given - 탈퇴한 유저 Mock 데이터 생성
        User withdrawnUser = new User("test", "newPassword123!", "newemail@test.com", "newNickname");
        withdrawnUser.deactivate(); // 탈퇴 처리
        ResWithdrawUserDTO res = ResWithdrawUserDTO.from(withdrawnUser);
        CommonResponse<ResWithdrawUserDTO> commonResponse = new CommonResponse<>(true, "회원 탈퇴 성공", res);

        // When & Then
        when(userService.withdrawUser(any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenReturn(commonResponse);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/withdraw")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("test"))
                .andExpect(jsonPath("$.data.deletedAt").exists());
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 존재하지 않는 URL 요청 시 404 Not Found 응답")
    void nonExistingUrl() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/non-existing-endpoint"))
                .andDo(print())
                .andExpect(status().isBadRequest()); // TODO: 원인 파악
    }

    @Test
    @DisplayName("[실패] 회원가입 Controller - 잘못된 HTTP 메서드 요청 시 405 Method Not Supported 응답")
    void signup_wrongHttpMethod() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/user/signup"))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("[실패] 회원정보 리스트 조회 Controller - 로그인 없이 호출 시 403 Access Denied 응답")
    void getUserList_unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/list")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test", roles = "USER")
    @DisplayName("[실패] 회원정보 리스트 조회 Controller - 일반 유저 호출 시 403 Access Denied 응답")
    void getUserList_unauthorized_user() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/list")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test", roles = "ADMIN")
    @DisplayName("[실패] 회원정보 리스트 조회 Controller - 잘못된 HTTP 메서드로 요청 시 405 Method Not Allowed 응답")
    void getUserList_wrongHttpMethod() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/list")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("[실패] 회원정보 상세 조회 Controller - 로그인 없이 호출 시 403 Access Denied 응답")
    void getUserDetailById_unauthorized() throws Exception {
        Long id = 1L;
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 회원정보 상세 조회 Controller - 잘못된 HTTP 메서드로 요청 시 405 Method Not Allowed 응답")
    void getUserDetailById_wrongHttpMethod() throws Exception {
        Long id = 1L;
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/user/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("[실패] 회원정보 수정 Controller - 로그인 없이 호출 시 403 Access Denied 응답")
    void updateUserInfo_unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/update")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 회원정보 수정 Controller - 잘못된 HTTP 메서드로 요청 시 405 Method Not Allowed 응답")
    void updateUserInfo_wrongHttpMethod() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/user/update"))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 회원정보 수정 Controlle - request body 미전달로 400 Bad Request 응답")
    void updateUserInfo_badRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/update")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[실패] 회원탈퇴 Controller - 로그인 없이 호출 시 403 Access Denied 응답")
    void withdrawUser_unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/withdraw")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 회원탈퇴 Controller - 잘못된 HTTP 메서드로 요청 시 405 Method Not Allowed 응답")
    void withdrawUser_wrongHttpMethod() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/user/withdraw")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }

}
