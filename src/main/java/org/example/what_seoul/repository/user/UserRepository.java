package org.example.what_seoul.repository.user;

import org.example.what_seoul.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);
    Optional<User> findByEmail(String email);
    Optional<User> findByNickName(String nickName);
    List<User> findByDeletedAtBefore(LocalDateTime deadline);
    boolean existsByEmail(String Email);

    boolean existsByNickName(String nickName);

}
