package org.example.what_seoul.repository.board;

import org.example.what_seoul.domain.board.Board;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {
    Slice<Board> findSliceByCultureEventId(Long cultureEventId, Pageable pageable);

    List<Board> findAllByUserId(Long userId);

    Slice<Board> findSliceByUserId(Long userId, Pageable pageable);

    boolean existsByCultureEventId(Long cultureEventId);
}
