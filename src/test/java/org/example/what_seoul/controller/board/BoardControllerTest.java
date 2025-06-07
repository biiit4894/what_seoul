package org.example.what_seoul.controller.board;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.config.WebSecurityTestConfig;
import org.example.what_seoul.controller.board.dto.*;
import org.example.what_seoul.service.board.BoardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BoardController.class)
@ActiveProfiles("test")
@Import(WebSecurityTestConfig.class)
public class BoardControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BoardService boardService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[성공] 문화행사 후기 작성 Controller")
    void createBoard() throws Exception {
        // given
        ReqCreateBoardDTO request = new ReqCreateBoardDTO("test", 1L);
        ResCreateBoardDTO responseDTO = new ResCreateBoardDTO(1L, "test", LocalDateTime.now(), "nickname", "행사명");

        CommonResponse<ResCreateBoardDTO> response = new CommonResponse<>(true, "문화행사 후기 작성 성공", responseDTO);

        given(boardService.createBoard(any(ReqCreateBoardDTO.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/board")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("문화행사 후기 작성 성공"))
                .andExpect(jsonPath("$.data.content").value("test"));
    }

    @DisplayName("[성공] 문화행사 후기 리스트 조회 Controller")
    @WithMockUser(username = "test", roles = {"ADMIN","USER"})
    @Test
    void getBoardsByCultureEventId() throws Exception {
        // given
        Long cultureEventId = 1L;
        int page = 0;
        int size = 2;

        List<ResGetBoardDTO> content = List.of(
                new ResGetBoardDTO(1L, "후기 내용1", LocalDateTime.now(), LocalDateTime.now(), "작성자1", "행사1", true),
                new ResGetBoardDTO(2L, "후기 내용2", LocalDateTime.now(), LocalDateTime.now(), "작성자2", "행사1", false)
        );

        Slice<ResGetBoardDTO> slice = new SliceImpl<>(content, PageRequest.of(page, size), false);
        CommonResponse<Slice<ResGetBoardDTO>> response = new CommonResponse<>(true, "장소별 문화행사 후기 목록 조회 성공", slice);

        given(boardService.getBoardsByCultureEventId(eq(cultureEventId), eq(page), eq(size)))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/board")
                        .param("cultureEventId", String.valueOf(cultureEventId))
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("장소별 문화행사 후기 목록 조회 성공"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].content").value("후기 내용1"))
                .andExpect(jsonPath("$.data.content[0].author").value("작성자1"))
                .andExpect(jsonPath("$.data.content[0].eventName").value("행사1"));
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[성공] 문화행사 후기 조회 Controller")
    void getBoardById() throws Exception {
        // given
        ResGetBoardDTO responseDTO = new ResGetBoardDTO(1L, "test", LocalDateTime.of(2025, 1, 1, 10, 0), LocalDateTime.of(2025, 1, 1, 11, 30), "nickName", "test", false);
        CommonResponse<ResGetBoardDTO> response = new CommonResponse<>(true, "문화행사 후기 조회 성공", responseDTO);

        given(boardService.getBoardById(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/board/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("문화행사 후기 조회 성공"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.updatedAt").exists())
                .andExpect(jsonPath("$.data.content").value("test"))
                .andExpect(jsonPath("$.data.author").value("nickName"))
                .andExpect(jsonPath("$.data.eventName").value("test"))
                .andExpect(jsonPath("$.data.editable").value(false)); // Java/Kotlin에서 boolean 필드명이 isXyz로 시작되면, Jackson(JSON 직렬화 라이브러리)은 is를 제외한 xyz로 직렬화한다.

    }

    @DisplayName("[성공] 작성한 문화행사 후기 리스트 조회 Controller")
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @Test
    void getBoardsByUserId() throws Exception {
        // given
        int page = 0;
        int size = 10;

        String sort = "desc";
        String startDate = "2025-06-01";
        String endDate = "2025-06-02";

        ReqGetMyBoardDTO req = new ReqGetMyBoardDTO(List.of("areaName1", "areaName2"));

        List<ResGetMyBoardDTO> boardList = List.of(
                new ResGetMyBoardDTO(
                        1L,
                        "test content",
                        LocalDateTime.of(2024, 5, 1, 10, 0),
                        LocalDateTime.of(2024, 5, 2, 11, 0),
                        "test event name",
                        "test event place",
                        "https://testurl.com",
                        "test area name",
                        false
                )
        );
        Slice<ResGetMyBoardDTO> boardSlice = new SliceImpl<>(boardList, PageRequest.of(page, size), false);
        CommonResponse<Slice<ResGetMyBoardDTO>> response = new CommonResponse<>(true, "작성한 문화행사 후기 목록 조회 성공", boardSlice);

        given(boardService.getMyBoards(
                eq(page),
                eq(size),
                eq(LocalDate.parse(startDate)),
                eq(LocalDate.parse(endDate)),
                eq(sort),
                any(ReqGetMyBoardDTO.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/board/my")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .param("startDate", startDate)
                        .param("endDate", endDate)
                        .param("sort", sort)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("작성한 문화행사 후기 목록 조회 성공"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(1L))
                .andExpect(jsonPath("$.data.content[0].content").value("test content"))
                .andExpect(jsonPath("$.data.content[0].eventName").value("test event name"))
                .andExpect(jsonPath("$.data.content[0].eventPlace").value("test event place"))
                .andExpect(jsonPath("$.data.content[0].areaName").value("test area name"))
                .andExpect(jsonPath("$.data.content[0].ended").value(false));
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[성공] 문화행사 후기 수정 Controller")
    void updateBoard() throws Exception {
        // given
        ReqUpdateBoardDTO requestDTO = new ReqUpdateBoardDTO("update");
        ResUpdateBoardDTO responseDTO = new ResUpdateBoardDTO(
                1L,
                "update",
                LocalDateTime.of(2025, 1, 1, 10, 0),
                LocalDateTime.of(2025, 1, 1, 11, 30),
                "nickName",
                "test"
        );

        CommonResponse<ResUpdateBoardDTO> response = new CommonResponse<>(true, "문화행사 후기 수정 성공", responseDTO);

        given(boardService.updateBoard(eq(1L), any(ReqUpdateBoardDTO.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(put("/api/board/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("문화행사 후기 수정 성공"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.content").value("update"))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.updatedAt").exists())
                .andExpect(jsonPath("$.data.author").value("nickName"))
                .andExpect(jsonPath("$.data.eventName").value("test"));
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[성공] 문화행사 후기 삭제 Controller")
    void deleteBoard() throws Exception {
        ResDeleteBoardDTO responseDTO = new ResDeleteBoardDTO(
                1L,
                "test",
                LocalDateTime.of(2025, 1, 1, 10, 0),
                LocalDateTime.of(2025, 1, 1, 11, 30),
                "nickName",
                "test"
        );
        CommonResponse<ResDeleteBoardDTO> response = new CommonResponse<>(true, "문화행사 후기 삭제 성공", responseDTO);

        given(boardService.deleteBoard(1L)).willReturn(response);

        // when & then
        mockMvc.perform(delete("/api/board/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("문화행사 후기 삭제 성공"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.content").value("test"))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.updatedAt").exists())
                .andExpect(jsonPath("$.data.author").value("nickName"))
                .andExpect(jsonPath("$.data.eventName").value("test"));
    }

    @Test
    @DisplayName("[실패] 문화행사 후기 작성 Controller - 로그인 없이 호출 시 403 Access Denied 응답")
    void createBoard_unauthorized() throws Exception {
        ReqCreateBoardDTO request = new ReqCreateBoardDTO("test", 1L);

        mockMvc.perform(post("/api/board")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 문화행사 후기 작성 Controller - 잘못된 HTTP 메서드로 요청 시 405 Method Not Allowed 응답")
    void createBoard_wrongHttpMethod() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/board"))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 문화행사 후기 목록 조회 Controller - cultureEventId 누락 시 400 Bad Request 응답")
    void getBoardsByCultureEventId_missingParam() throws Exception {
        mockMvc.perform(get("/api/board")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[실패] 작성한 문화행사 후기 목록 조회 Controller - 인증 없이 후기 목록 요청 시 403 Access Denied 반환")
    void getBoardsByUserId_unauthorized() throws Exception {
        mockMvc.perform(post("/api/board/my")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 문화행사 후기 단건 조회 Controller - 존재하지 않는 ID로 조회 시 404 Not Found 응답")
    void getBoardById_notFound() throws Exception {
        given(boardService.getBoardById(anyLong()))
                .willThrow(new EntityNotFoundException("문화행사 후기를 찾을 수 없습니다."));

        mockMvc.perform(get("/api/board/{id}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("[실패] 문화행사 후기 수정 Controller - 로그인 없이 호출 시 403 Forbidden 응답")
    void updateBoard_unauthorized() throws Exception {
        ReqUpdateBoardDTO request = new ReqUpdateBoardDTO("수정 내용");

        mockMvc.perform(put("/api/board/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test", roles = {"USER"})
    @DisplayName("[실패] 문화행사 후기 삭제 Controller - 작성자가 아닌 유저가 삭제 요청 시 403 Forbidden 응답")
    void deleteBoard_accessDenied() throws Exception {
        given(boardService.deleteBoard(1L))
                .willThrow(new AccessDeniedException("삭제 권한이 없습니다."));

        mockMvc.perform(delete("/api/board/{id}", 1L))
                .andDo(print())
                .andExpect(status().isForbidden());
    }


}

