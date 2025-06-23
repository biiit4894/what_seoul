package org.example.what_seoul.service.board;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.common.validation.CustomValidator;
import org.example.what_seoul.controller.board.dto.*;
import org.example.what_seoul.domain.board.Board;
import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.domain.citydata.event.CultureEvent;
import org.example.what_seoul.domain.user.RoleType;
import org.example.what_seoul.domain.user.User;
import org.example.what_seoul.exception.CustomValidationException;
import org.example.what_seoul.repository.board.BoardRepository;
import org.example.what_seoul.repository.citydata.event.CultureEventRepository;
import org.example.what_seoul.service.user.UserService;
import org.example.what_seoul.service.user.dto.LoginUserInfoDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class BoardServiceTest {
    @InjectMocks
    private BoardService boardService;

    @Mock
    private UserService userService;

    @Mock
    private CultureEventRepository cultureEventRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private CustomValidator customValidator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public BoardServiceTest() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("[성공] 문화행사 후기 작성 Service")
    void createBoard() throws JsonProcessingException {
        // given
        User user = new User("test", "encodedPassword", "test@example.com", "testNickName", RoleType.USER);
        Area area = new Area(
                "POI050",
                "천호역",
                "인구밀집지역",
                "POLYGON ((127.12782542009622 37.54031451897783, 127.12784181342528 37.53998366374988, 127.12741614871513 37.53897810766761, 127.12652374918184 37.53772998648546, 127.12591148105541 37.536942465411364, 127.12493751476998 37.53731306443991, 127.12392921331896 37.537719234202996, 127.12339549385793 37.53798986463069, 127.12255616680405 37.538038001191474, 127.12199691548906 37.53825463748036, 127.12249874785249 37.53921962356995, 127.12290693539497 37.53994839236029, 127.12334881386708 37.54053533696927, 127.123391521014 37.54072434233456, 127.12368900480186 37.541156145338256, 127.12396925407013 37.54141917105127, 127.12782542009622 37.54031451897783))"
        );
        CultureEvent event = new CultureEvent(
                "행사1",
                "2025-04-25~2025-09-07",
                "주소1",
                "127.00977973484339",
                "37.56735731522952",
                "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=0bdf8a8555544da9a012da6ea6f49e50&thumb=Y",
                "https://culture.seoul.go.kr/culture/culture/cultureEvent/view.do?cultcode=152681&menuNo=200009",
                area
        );
        ReflectionTestUtils.setField(event, "id", 10L); // 테스트 환경에서 id 값 설정하기

        ReqCreateBoardDTO req = new ReqCreateBoardDTO("test", 10L);
        Board savedBoard = new Board(req.getContent(), user, event);


        given(userService.getAuthenticationPrincipal()).willReturn(user);
        given(cultureEventRepository.findById(10L)).willReturn(Optional.of(event));
        given(boardRepository.save(any(Board.class))).willReturn(savedBoard);

        // when
        CommonResponse<ResCreateBoardDTO> response = boardService.createBoard(req);

        // then
        assertTrue(response.isSuccess());
        assertEquals("문화행사 후기 작성 성공", response.getMessage());

        ResCreateBoardDTO data = response.getData();
        assertEquals("test", data.getContent());
        assertEquals("testNickName", data.getAuthor());
        assertEquals("행사1", data.getEventName());

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(json);
    }

    @Test
    @DisplayName("[성공] 문화행사 후기 목록 조회 Service")
    void getBoardsByCultureEventId() throws JsonProcessingException {
        // given
        Long cultureEventId = 1L;
        int page = 0;
        int size = 2;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        User user = new User("test", "encodedPassword", "test@example.com", "작성자", RoleType.USER);
        ReflectionTestUtils.setField(user, "id", 1L); // 테스트 환경에서 id 값 설정하기

        Area area = new Area(
                "POI050",
                "천호역",
                "인구밀집지역",
                "POLYGON ((127.12782542009622 37.54031451897783, 127.12784181342528 37.53998366374988, 127.12741614871513 37.53897810766761, 127.12652374918184 37.53772998648546, 127.12591148105541 37.536942465411364, 127.12493751476998 37.53731306443991, 127.12392921331896 37.537719234202996, 127.12339549385793 37.53798986463069, 127.12255616680405 37.538038001191474, 127.12199691548906 37.53825463748036, 127.12249874785249 37.53921962356995, 127.12290693539497 37.53994839236029, 127.12334881386708 37.54053533696927, 127.123391521014 37.54072434233456, 127.12368900480186 37.541156145338256, 127.12396925407013 37.54141917105127, 127.12782542009622 37.54031451897783))"
        );
        CultureEvent event = new CultureEvent(
                "행사1",
                "2025-04-25~2025-09-07",
                "주소1",
                "127.00977973484339",
                "37.56735731522952",
                "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=0bdf8a8555544da9a012da6ea6f49e50&thumb=Y",
                "https://culture.seoul.go.kr/culture/culture/cultureEvent/view.do?cultcode=152681&menuNo=200009",
                area
        );

        Board board1 = new Board("내용1", user, event);
        Board board2 = new Board("내용2", user, event);

        Slice<Board> boardSlice = new SliceImpl<>(List.of(board1, board2), pageable, false);

        given(boardRepository.findSliceByCultureEventId(cultureEventId, pageable)).willReturn(boardSlice);
        given(userService.getLoginUserInfo()).willReturn(new LoginUserInfoDTO(user.getId(), user.getUserId(), user.getEmail(), user.getNickName(), user.getRole(), user.getCreatedAt(), user.getUpdatedAt(), user.getDeletedAt()));

        // when
        CommonResponse<Slice<ResGetBoardDTO>> response = boardService.getBoardsByCultureEventId(cultureEventId, page, size);

        // then
        assertTrue(response.isSuccess());
        assertEquals("장소별 문화행사 후기 목록 조회 성공", response.getMessage());
        assertEquals(2, response.getData().getContent().size());

        ResGetBoardDTO dto = response.getData().getContent().get(0);
        assertEquals("내용1", dto.getContent());
        assertEquals("작성자", dto.getAuthor());
        assertEquals("행사1", dto.getEventName());
        assertTrue(dto.isEditable());

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(json);
    }

    @Test
    @DisplayName("[성공] 문화행사 후기 목록 조회 Service - 문화행사 ID에 해당하는 후기이 없는 경우 빈 Slice 반환")
    void getBoardsByCultureEventId_noBoards() throws JsonProcessingException {
        // given
        Long cultureEventId = 999L; // 존재하지 않는 문화행사 ID
        int page = 0;
        int size = 2;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Slice<Board> emptySlice = new SliceImpl<>(Collections.emptyList(), pageable, false);

        given(boardRepository.findSliceByCultureEventId(cultureEventId, pageable)).willReturn(emptySlice);
        given(userService.getLoginUserInfo()).willReturn(new LoginUserInfoDTO(1L, "test", "test@example.com", "작성자", RoleType.USER, LocalDateTime.now(), null, null));

        // when
        CommonResponse<Slice<ResGetBoardDTO>> response = boardService.getBoardsByCultureEventId(cultureEventId, page, size);

        // then
        assertTrue(response.isSuccess());
        assertEquals("장소별 문화행사 후기 목록 조회 성공", response.getMessage());
        assertTrue(response.getData().isEmpty());

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(json);
    }

    @Test
    @DisplayName("[성공] 문화행사 후기 조회 Service")
    void getBoardById() throws JsonProcessingException {
        // given
        User user = new User("testUser", "encodedPassword", "test@example.com", "작성자", RoleType.USER);
        ReflectionTestUtils.setField(user, "id", 10L);

        Area area = new Area("POI001", "강남역", "인구밀집지역", "POLYGON ((...))");
        CultureEvent event = new CultureEvent("행사명", "2025-01-01~2025-12-31", "주소", "127.123", "37.456", "imageUrl", "detailUrl", area);

        Board board = new Board("후기 내용입니다", user, event);
        ReflectionTestUtils.setField(board, "id", 1L);

        LoginUserInfoDTO loginUser = new LoginUserInfoDTO(
                user.getId(), user.getUserId(), user.getEmail(), user.getNickName(), RoleType.USER,
                LocalDateTime.now(), null, null
        );

        given(boardRepository.findById(1L)).willReturn(Optional.of(board));
        given(userService.getLoginUserInfo()).willReturn(loginUser);

        // when
        CommonResponse<ResGetBoardDTO> response = boardService.getBoardById(1L);

        // then
        assertTrue(response.isSuccess());
        assertEquals("문화행사 후기 조회 성공", response.getMessage());

        ResGetBoardDTO dto = response.getData();
        assertEquals(1L, dto.getId());
        assertEquals("후기 내용입니다", dto.getContent());
        assertEquals("작성자", dto.getAuthor());
        assertEquals("행사명", dto.getEventName());
        assertTrue(dto.isEditable());

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(json);
    }

    @DisplayName("[성공] 작성한 문화행사 후기 목록 조회 Service")
    @Test
    void getBoardsByUserId_success() throws JsonProcessingException {
        // given
        int page = 0;
        int size = 10;
        String sort = "desc";
        LocalDate nowDate = LocalDate.of(2024, 6, 1);
        LocalDate startDate = nowDate.minusDays(3);
        LocalDate endDate = nowDate.plusDays(1);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        Sort.Direction direction = Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size);

        Long userId = 1L;

        ReqGetMyBoardDTO req = new ReqGetMyBoardDTO(List.of("areaName1", "areaName2"));

        given(userService.getLoginUserInfo()).willReturn(new LoginUserInfoDTO(userId, "testUser", "testEmail", "testNickName", RoleType.USER, LocalDateTime.now(), null, null));

        List<ResGetMyBoardDTO> content = List.of(
                new ResGetMyBoardDTO(1L, "content", startDateTime, endDateTime,
                        "event", "place", "url", "areaName1", false)
        );
        Slice<ResGetMyBoardDTO> slice = new SliceImpl<>(content, pageable, false);
        given(boardRepository.findMyBoardsSlice(
                eq(userId),
                eq(startDateTime),
                eq(endDateTime),
                eq(req.getSelectedAreaNames()),
                eq(pageable),
                eq(direction))).willReturn(slice);

        // when
        CommonResponse<Slice<ResGetMyBoardDTO>> result = boardService.getMyBoards(page, size, startDate, endDate, sort, req);

        // then
        assertTrue(result.isSuccess());
        assertEquals("작성한 문화행사 후기 목록 조회 성공", result.getMessage());
        assertEquals(1, result.getData().getContent().size());
        assertEquals("content", result.getData().getContent().get(0).getContent());
        assertEquals("areaName1", result.getData().getContent().get(0).getAreaName());

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        System.out.println(json);
    }

    @Test
    @DisplayName("[성공] 작성한 문화행사 후기 목록 조회 Service - Request Body가 null인 경우")
    void getBoardsByUserId_reqBodyIsNull_success() throws JsonProcessingException {
        // given
        int page = 0;
        int size = 10;

        String sort = "desc";
        LocalDate nowDate = LocalDate.of(2024, 6, 1);
        LocalDate startDate = nowDate.minusDays(3);
        LocalDate endDate = nowDate.plusDays(1);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        ReqGetMyBoardDTO req = null;

        Long userId = 1L;
        Pageable pageable = PageRequest.of(page, size);

        given(userService.getLoginUserInfo()).willReturn(new LoginUserInfoDTO(userId, "testUser", "testEmail", "testNickName", RoleType.USER, LocalDateTime.now(), null, null));

        Slice<ResGetMyBoardDTO> emptySlice = new SliceImpl<>(List.of(), pageable, false);

        given(boardRepository.findMyBoardsSlice(
                eq(userId),
                eq(startDateTime),
                eq(endDateTime),
                isNull(),
                eq(pageable),
                eq(Sort.Direction.DESC))
        ).willReturn(emptySlice);

        // when
        CommonResponse<Slice<ResGetMyBoardDTO>> result = boardService.getMyBoards(
                page, size, startDate, endDate, sort, req
        );

        // then
        assertTrue(result.isSuccess());
        assertEquals("작성한 문화행사 후기 목록 조회 성공", result.getMessage());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        System.out.println(json);
    }



    @Test
    @DisplayName("[성공] 문화행사 후기 수정 Service")
    void updateBoard() throws JsonProcessingException {
        // given
        ReqUpdateBoardDTO req = new ReqUpdateBoardDTO("update");

        User user = new User("testUser", "encodedPassword", "test@example.com", "작성자", RoleType.USER);
        ReflectionTestUtils.setField(user, "id", 10L);

        Area area = new Area("POI001", "강남역", "인구밀집지역", "POLYGON ((...))");
        CultureEvent event = new CultureEvent("행사명", "2025-01-01~2025-12-31", "주소", "127.123", "37.456", "imageUrl", "detailUrl", area);

        Board board = new Board("test", user, event);
        ReflectionTestUtils.setField(board, "id", 1L);

        given(boardRepository.findById(1L)).willReturn(Optional.of(board));
        given(userService.getAuthenticationPrincipal()).willReturn(user);
        given(customValidator.validate(req)).willReturn(Collections.emptySet());

        // when
        CommonResponse<ResUpdateBoardDTO> response = boardService.updateBoard(1L, req);

        // then
        assertTrue(response.isSuccess());
        assertEquals("문화행사 후기 수정 성공", response.getMessage());
        assertEquals("update", response.getData().getContent());
        assertEquals(board.getCreatedAt(), response.getData().getCreatedAt());
        assertEquals(board.getUpdatedAt(), response.getData().getUpdatedAt());
        assertEquals(board.getUser().getNickName(), response.getData().getAuthor());
        assertEquals(board.getCultureEvent().getEventName(), response.getData().getEventName());

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(json);
    }


    @Test
    @DisplayName("[성공] 문화행사 후기 삭제 Service")
    void deleteBoard() {
        // given
        User user = new User("testUser", "encodedPassword", "test@example.com", "작성자", RoleType.USER);
        ReflectionTestUtils.setField(user, "id", 10L);

        Area area = new Area("POI001", "강남역", "인구밀집지역", "POLYGON ((...))");
        CultureEvent event = new CultureEvent("행사명", "2025-01-01~2025-12-31", "주소", "127.123", "37.456", "imageUrl", "detailUrl", area);

        Board board = new Board("test", user, event);
        ReflectionTestUtils.setField(board, "id", 1L);

        given(boardRepository.findById(1L)).willReturn(Optional.of(board));
        given(userService.getAuthenticationPrincipal()).willReturn(user);

        // when
        CommonResponse<ResDeleteBoardDTO> response = boardService.deleteBoard(1L);

        // then
        assertTrue(response.isSuccess());
        assertEquals("문화행사 후기 삭제 성공", response.getMessage());
        assertEquals("test", response.getData().getContent());
        assertEquals(board.getCreatedAt(), response.getData().getCreatedAt());
        assertEquals(board.getUpdatedAt(), response.getData().getUpdatedAt());
        assertEquals(board.getUser().getNickName(), response.getData().getAuthor());
        assertEquals(board.getCultureEvent().getEventName(), response.getData().getEventName());
    }


    @Test
    @DisplayName("[실패] 문화행사 후기 작성 Service - 존재하지 않는 문화행사 ID로 후기를 작성하려는 행사를 조회할 경우 EntityNotFoundException 발생")
    void createBoard_eventNotFound() {
        // given
        ReqCreateBoardDTO req = new ReqCreateBoardDTO("후기 내용입니다.", 999L);
        User user = new User("test", "encodedPassword", "test@example.com", "testNickName", RoleType.USER);

        given(userService.getAuthenticationPrincipal()).willReturn(user);
        given(cultureEventRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> boardService.createBoard(req));

        assertEquals("문화행사를 찾을 수 없습니다. 문화행사 id = 999", exception.getMessage());
    }

    @Test
    @DisplayName("[실패] 문화행사 후기 조회 Service - 존재하지 않는 후기 ID로 조회 시 EntityNotFoundException 발생")
    void getBoardById_boardNotFound() {
        // given
        Long invalidId = 999L;

        given(boardRepository.findById(invalidId)).willReturn(Optional.empty());

        // when & then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> {
            boardService.getBoardById(invalidId);
        });

        assertEquals("문화행사 후기를 찾을 수 없습니다. 후기 id = 999", ex.getMessage());
    }

    @Test
    @DisplayName("[실패] 작성한 문화행사 후기 목록 조회 Service - 잘못된 페이지 파라미터 전달 시 IllegalArgumentException 발생")
    void getBoardsByUserId_invalidPageParam() {
        // given
        int invalidPage = -1;
        int size = 10;

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            boardService.getMyBoards(invalidPage, size, null, null, null, null);
        });
    }

    @Test
    @DisplayName("[실패] 작성한 문화행사 후기 목록 조회 Service - 종료일이 시작일보다 빠른 경우 IllegalArgumentException 발생")
    void getMyBoards_endDateBeforeStartDate() {
        // given
        int page = 0;
        int size = 10;
        LocalDate startDate = LocalDate.of(2024, 6, 5);
        LocalDate endDate = LocalDate.of(2024, 6, 1);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            boardService.getMyBoards(page, size, startDate, endDate, "desc", new ReqGetMyBoardDTO(List.of("서울")));
        });
    }

    @Test
    @DisplayName("[실패] 문화행사 후기 수정 Service - 존재하지 않는 후기 ID로 수정 시 EntityNotFoundException 발생")
    void updateBoard_boardNotFound() {
        // given
        Long id = 999L;
        ReqUpdateBoardDTO req = new ReqUpdateBoardDTO("test.");

        given(boardRepository.findById(id)).willReturn(Optional.empty());

        // when & then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> {
            boardService.updateBoard(id, req);
        });

        assertEquals("문화행사 후기를 찾을 수 없습니다. 후기 id = 999", ex.getMessage());
    }

    @Test
    @DisplayName("[실패] 문화행사 후기 수정 Service - 본인 글이 아닐 경우 AccessDeniedException 발생")
    void updateBoard_accessDenied() {
        Long id = 1L;
        ReqUpdateBoardDTO req = new ReqUpdateBoardDTO("update");

        User currentUser = new User("current", "pw", "email", "nick", RoleType.USER);
        ReflectionTestUtils.setField(currentUser, "id", 1L);

        User boardOwner = new User("owner", "pw", "email2", "nick2", RoleType.USER);
        ReflectionTestUtils.setField(boardOwner, "id", 2L);

        Board board = mock(Board.class);

        given(boardRepository.findById(id)).willReturn(Optional.of(board));
        given(board.getUser()).willReturn(boardOwner);
        given(userService.getAuthenticationPrincipal()).willReturn(currentUser);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> boardService.updateBoard(id, req));

        assertEquals("수정 권한이 없습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("[실패] 문화행사 후기 수정 Service - 같은 내용으로 수정 시 CustomValidationException 발생")
    void updateBoard_sameContent() {
        Long id = 1L;
        String content = "same content";
        ReqUpdateBoardDTO req = new ReqUpdateBoardDTO(content);

        User user = new User("test", "pw", "email", "nick", RoleType.USER);

        Board board = mock(Board.class);
        given(boardRepository.findById(id)).willReturn(Optional.of(board));
        given(userService.getAuthenticationPrincipal()).willReturn(user);
        given(board.getUser()).willReturn(user);
        given(board.getContent()).willReturn(content); // 수정 전과 동일한 내용

        // 검증 로직 수정
        Set<ConstraintViolation<ReqUpdateBoardDTO>> violations = new HashSet<>();

        // Mock ConstraintViolation 생성
        ConstraintViolation<ReqUpdateBoardDTO> violation = Mockito.mock(ConstraintViolation.class);

        // PropertyPath mock 생성
        Path propertyPath = mock(Path.class);

        given(violation.getPropertyPath()).willReturn(propertyPath);  // PropertyPath mock
        given(propertyPath.toString()).willReturn("content"); // content 경로로 설정

        given(violation.getMessage()).willReturn("기존과 동일한 내용입니다.");
        violations.add(violation);

        // 이제 customValidator의 validate 메서드가 제대로 mock됩니다.
        given(customValidator.validate(req)).willReturn(violations);

        // CustomValidationException 예외 발생 확인
        CustomValidationException ex = assertThrows(CustomValidationException.class,
                () -> boardService.updateBoard(id, req));

        assertTrue(ex.getErrors().get("content").contains("기존과 동일한 내용입니다."));

    }

    @Test
    @DisplayName("[실패] 문화행사 후기 수정 Service - 후기 길이 300자 초과로 실패")
    void updateBoard_contentTooLong() {
        // given
        String overLengthContent = "a".repeat(301); // 301자
        ReqUpdateBoardDTO req = new ReqUpdateBoardDTO(overLengthContent);

        User user = new User("testUser", "encodedPassword", "test@example.com", "작성자", RoleType.USER);
        ReflectionTestUtils.setField(user, "id", 10L);

        Board board = mock(Board.class);


        Set<ConstraintViolation<ReqUpdateBoardDTO>> violations = new HashSet<>();

        // ConstraintViolation mock 생성
        ConstraintViolation<ReqUpdateBoardDTO> violation = mock(ConstraintViolation.class);

        // PropertyPath mock 생성
        Path propertyPath = mock(Path.class);

        given(violation.getPropertyPath()).willReturn(propertyPath);  // PropertyPath mock
        given(propertyPath.toString()).willReturn("content"); // content 경로 설정

        given(violation.getMessage()).willReturn("후기는 300자 이하로 작성해야 합니다.");
        violations.add(violation);

        given(boardRepository.findById(1L)).willReturn(Optional.of(board));
        given(userService.getAuthenticationPrincipal()).willReturn(user);
        given(board.getUser()).willReturn(user);
        given(board.getContent()).willReturn("기존내용");
        given(customValidator.validate(req)).willReturn(violations);

        // when
        CustomValidationException ex = assertThrows(
                CustomValidationException.class,
                () -> boardService.updateBoard(1L, req)
        );

        // then
        assertTrue(ex.getErrors().get("content").contains("후기는 300자 이하로 작성해야 합니다."));
    }

    @Test
    @DisplayName("[실패] 문화행사 후기 삭제 Service - 존재하지 않는 후기 ID로 삭제 시 EntityNotFoundException 발생")
    void deleteBoard_boardNotFound() {
        // given
        given(boardRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> boardService.deleteBoard(1L)
        );

        assertTrue(exception.getMessage().contains("문화행사 후기를 찾을 수 없습니다"));
    }

    @Test
    @DisplayName("[실패] 문화행사 후기 삭제 Service - 일반 사용자가 다른 사용자의 후기 삭제를 시도할 경우 AccessDeniedException 발생")
    void deleteBoard_accessDenied() {
        Long id = 1L;

        User currentUser = new User("current", "pw", "email", "nick", RoleType.USER);
        ReflectionTestUtils.setField(currentUser, "id", 1L);

        User boardOwner = new User("owner", "pw", "email2", "nick2", RoleType.USER);
        ReflectionTestUtils.setField(boardOwner, "id", 2L);

        Board board = mock(Board.class);

        given(boardRepository.findById(id)).willReturn(Optional.of(board));
        given(board.getUser()).willReturn(boardOwner);
        given(userService.getAuthenticationPrincipal()).willReturn(currentUser);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> boardService.deleteBoard(id));

        assertEquals("삭제 권한이 없습니다.", ex.getMessage());
    }

}
