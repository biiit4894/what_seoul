package org.example.what_seoul.scheduler;

import org.example.what_seoul.config.WebSecurityTestConfig;
//import org.example.what_seoul.config.WebSecurityTestWithH2Config;
import org.example.what_seoul.config.WebSecurityTestWithH2Config;
import org.example.what_seoul.domain.board.Board;
import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.domain.citydata.event.CultureEvent;
import org.example.what_seoul.domain.user.RoleType;
import org.example.what_seoul.domain.user.User;
import org.example.what_seoul.repository.area.AreaRepository;
import org.example.what_seoul.repository.board.BoardRepository;
import org.example.what_seoul.repository.citydata.event.CultureEventRepository;
import org.example.what_seoul.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test-h2")
@Import(WebSecurityTestWithH2Config.class) // 테스트를 위한 custom security configuration
@Transactional
public class UserCleanupSchedulerTest {

    @Autowired
    private UserCleanupScheduler userCleanupScheduler;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private CultureEventRepository cultureEventRepository;

    private Area area;
    private CultureEvent cultureEvent;

    @BeforeEach
    void setUp() {
        area = new Area(
                "POI050",
                "천호역",
                "인구밀집지역",
                "POLYGON ((127.12782542009622 37.54031451897783, 127.12784181342528 37.53998366374988, 127.12741614871513 37.53897810766761, 127.12652374918184 37.53772998648546, 127.12591148105541 37.536942465411364, 127.12493751476998 37.53731306443991, 127.12392921331896 37.537719234202996, 127.12339549385793 37.53798986463069, 127.12255616680405 37.538038001191474, 127.12199691548906 37.53825463748036, 127.12249874785249 37.53921962356995, 127.12290693539497 37.53994839236029, 127.12334881386708 37.54053533696927, 127.123391521014 37.54072434233456, 127.12368900480186 37.541156145338256, 127.12396925407013 37.54141917105127, 127.12782542009622 37.54031451897783))"
        );
        areaRepository.save(area);

        cultureEvent = new CultureEvent(
                "행사1",
                "2025-04-25~2025-09-07",
                "주소1",
                "127.00977973484339",
                "37.56735731522952",
                "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=0bdf8a8555544da9a012da6ea6f49e50&thumb=Y",
                "https://culture.seoul.go.kr/culture/culture/cultureEvent/view.do?cultcode=152681&menuNo=200009",
                area
        );
        cultureEventRepository.save(cultureEvent);

    }
    @Test
    @DisplayName("[성공] 탈퇴 후 시간이 30일을 초과하여 지난 사용자를 삭제한다.")
    void shouldDeleteUsersSoftDeletedOver30DaysAgo() {
        // given
        String encodedPassword = encoder.encode("test1234!");
        User user = new User(
                "test",
                encodedPassword,
                "test@test.com",
                "testNickName",
                RoleType.USER
        );
        user.deactivate(); // 탈퇴 처리
        user.setDeletedAt(LocalDateTime.now().minusDays(30).minusSeconds(1)); // 탈퇴 시점에서 30일을 초과한 시간이 경과한 상태로 설정
        userRepository.save(user);

        Board board = new Board("test", user, cultureEvent);
        boardRepository.save(board);

        // when
        userCleanupScheduler.deleteUsers();

        // then
        assertThat(userRepository.findAll()).doesNotContain(user);
        assertThat(boardRepository.findAll()).doesNotContain(board);
    }

    @Test
    @DisplayName("[성공] 탈퇴 후 시간이 30일 미만으로 지난 사용자는 삭제되지 않는다.")
    void shouldNotDeleteUsersSoftDeletedWithin30Days() {
        // given
        String encodedPassword = encoder.encode("test1234!");
        User user = new User(
                "test",
                encodedPassword,
                "test@test.com",
                "testNickName",
                RoleType.USER
        );
        user.deactivate(); // 탈퇴 처리
        user.setDeletedAt(LocalDateTime.now().minusDays(10));
        userRepository.save(user);

        Board board = new Board("test", user, cultureEvent);
        boardRepository.save(board);

        // when
        userCleanupScheduler.deleteUsers();

        // then
        assertThat(userRepository.findAll()).contains(user);
        assertThat(boardRepository.findAll()).contains(board);
    }
}
