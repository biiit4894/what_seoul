package org.example.what_seoul.controller.board;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.board.dto.*;
import org.example.what_seoul.service.board.BoardService;
import org.springframework.data.domain.Slice;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/board")
@AllArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @PostMapping("")
    public ResponseEntity<CommonResponse<ResCreateBoardDTO>> createBoard(@Valid @RequestBody ReqCreateBoardDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boardService.createBoard(req));
    }

    @GetMapping("")
    public ResponseEntity<CommonResponse<Slice<ResGetBoardDTO>>> getBoardsByCultureEventId(
            @RequestParam(name = "cultureEventId") Long cultureEventId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(boardService.getBoardsByCultureEventId(cultureEventId, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<ResGetBoardDTO>> getBoardById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(boardService.getBoardById(id));
    }

    @GetMapping("/my")
    public ResponseEntity<CommonResponse<Slice<ResGetMyBoardDTO>>> getBoardsByUserId(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "sort", defaultValue = "desc") String sort
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(boardService.getBoardsByUserId(page, size, startDate, endDate, sort));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<ResUpdateBoardDTO>> updateBoard(@PathVariable Long id, @RequestBody ReqUpdateBoardDTO req) {
        return ResponseEntity.status(HttpStatus.OK).body(boardService.updateBoard(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<ResDeleteBoardDTO>> deleteBoard(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(boardService.deleteBoard(id));
    }

}
