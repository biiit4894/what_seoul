package org.example.what_seoul.repository;

import org.example.what_seoul.controller.board.dto.ResGetMyBoardDTO;
import org.example.what_seoul.domain.board.Board;
import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.domain.citydata.event.CultureEvent;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test-h2")
class BoardQueryRepositoryImplTest {

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
        Area area = new Area("POI001", "테스트지역", "유형", "POLYGON ((127.0 37.0, 127.1 37.1, 127.2 37.2, 127.0 37.0))");
        areaRepository.save(area);

        User user = new User("testuser", "encodedPw", "test@example.com", "testNick");
        userRepository.save(user);

        CultureEvent event = new CultureEvent(
                "테스트 행사", "2025-01-01~2025-12-31", "서울시 어딘가",
                "127.01", "37.01",
                "https://image.url", "https://event.url", area
        );
        cultureEventRepository.save(event);

        // 후기 15개 저장
        for (int i = 0; i < 15; i++) {
            Board board = new Board("후기 내용 " + i, user, event);
            boardRepository.save(board);
        }

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Slice<ResGetMyBoardDTO> result = boardRepository.findSliceByUserId(user.getId(), pageable);

        // Then
        assertNotNull(result);
        assertEquals(10, result.getContent().size());
        assertTrue(result.hasNext());

        ResGetMyBoardDTO first = result.getContent().get(0);
        assertEquals("테스트 행사", first.getEventName());
        assertEquals("후기 내용 14" , first.getContent());
    }
}

