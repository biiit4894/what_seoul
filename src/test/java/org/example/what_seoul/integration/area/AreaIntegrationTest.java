package org.example.what_seoul.integration.area;

import org.example.what_seoul.domain.board.Board;
import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.domain.citydata.event.CultureEvent;
import org.example.what_seoul.domain.user.RoleType;
import org.example.what_seoul.domain.user.User;
import org.example.what_seoul.repository.area.AreaRepository;
import org.example.what_seoul.repository.board.BoardRepository;
import org.example.what_seoul.repository.citydata.event.CultureEventRepository;
import org.example.what_seoul.repository.user.UserRepository;
import org.example.what_seoul.service.area.AreaService;
import org.example.what_seoul.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test-h2")
class AreaIntegrationTest {
    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private CultureEventRepository cultureEventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private AreaService areaService;

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("[성공] 후기를 작성한 장소 이름 목록 조회 통합 테스트")
    void getAreaNamesWithMyBoards() {
        // Given
        Area area1 = new Area("category", "A01", "홍대", "POLYGON ((127.0 37.0, 127.1 37.1, 127.2 37.2, 127.0 37.0))");
        Area area2 = new Area("category", "A02", "강남", "POLYGON ((127.0 37.0, 127.1 37.1, 127.2 37.2, 127.0 37.0))");
        areaRepository.saveAll(List.of(area1, area2));

        User user = new User("user1", "encodedPw", "user1@example.com", "닉네임1", RoleType.USER);
        userRepository.save(user);

        CultureEvent event1 = new CultureEvent("행사1", "2025-01-01~2025-12-31", "장소1", "127.01", "37.01", "img", "url", area1);
        CultureEvent event2 = new CultureEvent("행사2", "2025-01-01~2025-12-31", "장소2", "127.02", "37.02", "img", "url", area2);
        cultureEventRepository.saveAll(List.of(event1, event2));

        // 후기 등록
        boardRepository.save(new Board("후기1", user, event1));
        boardRepository.save(new Board("후기2", user, event2));
        boardRepository.save(new Board("후기3", user, event1));

        // When & Then
        List<String> areaNames = areaRepository.findAreaNamesByUserId(user.getId());
        assertEquals(2, areaNames.size());
        assertEquals(areaNames.get(0), "강남");
        assertEquals(areaNames.get(1), "홍대");
    }
}
