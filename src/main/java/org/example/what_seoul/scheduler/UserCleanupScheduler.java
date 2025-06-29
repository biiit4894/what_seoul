package org.example.what_seoul.scheduler;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.what_seoul.domain.board.Board;
import org.example.what_seoul.domain.user.User;
import org.example.what_seoul.repository.board.BoardRepository;
import org.example.what_seoul.repository.user.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    /**
     * 회원 탈퇴 기능과 연계되어, 매일 오전 3시마다 hard delete해야 하는 유저를 파악해 삭제하는 메소드.
     * - soft delete 처리(deletedAt 필드 값이 LocalDateTime.now()로 등록됨)된지 30일이 지난 유저들을 삭제한다.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteUsers() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(30);
        List<User> oldUsers = userRepository.findByDeletedAtBefore(deadline);
        oldUsers.forEach(user -> {
            List<Board> boards = boardRepository.findAllByUserId(user.getId());
            boardRepository.deleteAll(boards);
        });

        userRepository.deleteAll(oldUsers);
    }
}
