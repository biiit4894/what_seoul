package org.example.what_seoul.repository.board;

import org.example.what_seoul.controller.board.dto.ResGetMyBoardDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface BoardQueryRepository {
    Slice<ResGetMyBoardDTO> findSliceByUserId(Long userId, Pageable pageable);
}
