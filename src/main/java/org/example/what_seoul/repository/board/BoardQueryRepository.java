package org.example.what_seoul.repository.board;

import org.example.what_seoul.controller.board.dto.ResGetMyBoardDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

public interface BoardQueryRepository {
    Slice<ResGetMyBoardDTO> findMyBoardsSlice(Long userId, LocalDateTime startDateTime, LocalDateTime endDateTime, List<String> selectedAreaNames, Pageable pageable, Sort.Direction direction);
}
