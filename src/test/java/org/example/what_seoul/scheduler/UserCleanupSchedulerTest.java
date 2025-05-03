package org.example.what_seoul.scheduler;

import org.example.what_seoul.config.WebSecurityTestConfig;
//import org.example.what_seoul.config.WebSecurityTestWithH2Config;
import org.example.what_seoul.config.WebSecurityTestWithH2Config;
import org.example.what_seoul.domain.user.User;
import org.example.what_seoul.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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

    @Test
    @DisplayName("[성공] 탈퇴 후 시간이 30일을 초과하여 지난 사용자를 삭제한다.")
    void shouldDeleteUsersSoftDeletedOver30DaysAgo() {
        // given
        String rawPassword = "test1234!"; // 원본 비밀번호
        String encodedPassword = encoder.encode(rawPassword); // 암호화된 비밀번호

        User user = new User(
                "test",
                encodedPassword,
                "test@test.com",
                "testNickName"
        );
        LocalDateTime withdrawTime = LocalDateTime.now().minusDays(30).minusSeconds(1); // 탈퇴시각

        user.deactivate(); // 탈퇴 처리
        user.setDeletedAt(withdrawTime); // 탈퇴 시점에서 30일을 초과한 시간이 경과한 상태로 설정

        userRepository.save(user);

        // when
        userCleanupScheduler.deleteUsers();

        // then
        List<User> remainingUsers = userRepository.findAll();
        assertThat(remainingUsers).doesNotContain(user);
    }

    @Test
    @DisplayName("[성공] 탈퇴 후 시간이 30일 미만으로 지난 사용자는 삭제되지 않는다.")
    void shouldNotDeleteUsersSoftDeletedWithin30Days() {
        // given
        String rawPassword = "test1234!"; // 원본 비밀번호
        String encodedPassword = encoder.encode(rawPassword); // 암호화된 비밀번호

        User user = new User(
                "test",
                encodedPassword,
                "test@test.com",
                "testNickName"
        );
        user.setDeletedAt(LocalDateTime.now().minusDays(10));
        userRepository.save(user);

        // when
        userCleanupScheduler.deleteUsers();

        // then
        List<User> remainingUsers = userRepository.findAll();
        assertThat(remainingUsers).contains(user);
    }
}
