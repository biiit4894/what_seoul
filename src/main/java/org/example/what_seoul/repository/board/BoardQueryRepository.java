package org.example.what_seoul.repository.board;

import org.example.what_seoul.controller.board.dto.ResGetMyBoardDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;

public interface BoardQueryRepository {
    Slice<ResGetMyBoardDTO> findSliceByUserId(Long userId, LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);
}
