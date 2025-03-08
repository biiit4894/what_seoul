package org.example.what_seoul.util;

import lombok.RequiredArgsConstructor;
import org.example.what_seoul.entity.User;
import org.example.what_seoul.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;

    @Override
    public void run(String[] args) {
        if (userRepository.count() == 0) {
            // 테스트용 유저 100인 생성
            List<User> testUsers = new ArrayList<>();

            for (int i = 0; i < 100; i++) {
                testUsers.add(
                        new User(
                                "test" + (i + 1),
                                "test" + (i + 1),
                                "test" + (i + 1) + "@test" + (i + 1) + ".com",
                                "닉네임" + (i + 1),
                                LocalDateTime.now().minusDays(i + 1)
                        )
                );
            }

            userRepository.saveAll(testUsers);
        }
    }
}
