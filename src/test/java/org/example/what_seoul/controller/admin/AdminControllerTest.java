package org.example.what_seoul.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.config.JwtTokenProvider;
import org.example.what_seoul.config.WebSecurityTestConfig;
import org.example.what_seoul.controller.admin.dto.ReqCreateAdminDTO;
import org.example.what_seoul.controller.admin.dto.ResCreateAdminDTO;
import org.example.what_seoul.domain.user.RoleType;
import org.example.what_seoul.domain.user.User;
import org.example.what_seoul.service.admin.AdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@ActiveProfiles("test")
@Import({JwtTokenProvider.class, WebSecurityTestConfig.class})
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("[성공] 관리자 계정 생성 Controller")
    void signup() throws Exception {
        // Given
        ReqCreateAdminDTO req = new ReqCreateAdminDTO("admin", "password", "example@mail.com", "admin");
        User user = new User("admin", "password", "example@mail.com", "admin", RoleType.ADMIN);
        ResCreateAdminDTO res = ResCreateAdminDTO.from(user);
        CommonResponse<ResCreateAdminDTO> commonResponse = new CommonResponse<>(true, "관리자 계정 생성 성공", res);

        given(adminService.createAdminUser(anyString(), any(ReqCreateAdminDTO.class))).willReturn(commonResponse);

        String accessToken = "accessToken";

        // When & Then
        mockMvc.perform(post("/api/admin/signup")
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("admin"))
                .andExpect(jsonPath("$.data.email").value("example@mail.com"))
                .andExpect(jsonPath("$.data.nickName").value("admin"))
                .andExpect(jsonPath("$.data.createdAt").exists());

    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("[실패] 관리자 계정 생성 Controller - Request Body를 전달하지 않을 경우 400 응답")
    void signup_invalidRequest() throws Exception {
        String accessToken = "validAccessToken";

        mockMvc.perform(post("/api/admin/signup")
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
