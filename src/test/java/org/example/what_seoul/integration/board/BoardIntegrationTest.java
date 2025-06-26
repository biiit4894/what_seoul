package org.example.what_seoul.integration.board;

import org.example.what_seoul.controller.board.dto.ReqGetMyBoardDTO;
import org.example.what_seoul.controller.board.dto.ResGetMyBoardDTO;
import org.example.what_seoul.domain.board.Board;
import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.domain.citydata.event.CultureEvent;
import org.example.what_seoul.domain.user.RoleType;
import org.example.what_seoul.domain.user.User;
import org.example.what_seoul.repository.area.AreaRepository;
import org.example.what_seoul.repository.board.BoardRepository;
import org.example.what_seoul.repository.citydata.event.CultureEventRepository;
import org.example.what_seoul.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test-h2")
class BoardIntegrationTest {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CultureEventRepository cultureEventRepository;

    @Autowired
    private AreaRepository areaRepository;

    @Test
    @DisplayName("[성공] 작성한 후기 목록 조회 통합 테스트")
    void findSliceByUserId_success() {
        // Given
        Area area1 = new Area("category1", "areaCode1", "areaName1", "POLYGON ((127.0 37.0, 127.1 37.1, 127.2 37.2, 127.0 37.0))");
        Area area2 = new Area("category2", "areaCode2", "areaName2", "POLYGON ((127.0 37.0, 127.1 37.1, 127.2 37.2, 127.0 37.0))");
        areaRepository.saveAll(List.of(area1, area2));

        User user = new User("testuser", "encodedPw", "test@example.com", "testNick", RoleType.USER);
        userRepository.save(user);

        CultureEvent event1 = new CultureEvent(
                "테스트 행사1", "2025-01-01~2025-12-31", "서울시 어딘가",
                "127.01", "37.01",
                "https://image.url", "https://event.url", area1
        );
        CultureEvent event2 = new CultureEvent(
                "테스트 행사2", "2025-01-01~2025-12-31", "서울시 어딘가",
                "127.01", "37.01",
                "https://image.url", "https://event.url", area2
        );
        cultureEventRepository.saveAll(List.of(event1, event2));

        // 후기 15개 저장
        for (int i = 0; i < 15; i++) {
            CultureEvent event = (i % 2 == 0) ? event1 : event2;
            Board board = new Board("후기 내용 " + i, user, event);
            boardRepository.save(board);
        }

        Pageable pageable = PageRequest.of(0, 10);
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.minusDays(1);
        LocalDate endDate = now.plusDays(1);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        Sort.Direction direction = Sort.Direction.DESC;

        ReqGetMyBoardDTO req = new ReqGetMyBoardDTO(List.of("areaName1", "areaName2"));

        // When
        Slice<ResGetMyBoardDTO> result = boardRepository.findMyBoardsSlice(user.getId(), startDateTime, endDateTime, req.getSelectedAreaNames(), pageable, direction);

        // Then
        assertNotNull(result);
        assertEquals(10, result.getContent().size());
        assertTrue(result.hasNext());

        ResGetMyBoardDTO first = result.getContent().get(0);
        assertEquals("테스트 행사1", first.getEventName());
        assertEquals("후기 내용 14" , first.getContent());
    }
}

