package org.example.what_seoul.controller.board;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.board.dto.*;
import org.example.what_seoul.service.board.BoardService;
import org.example.what_seoul.swagger.operation.description.board.BoardDescription;
import org.example.what_seoul.swagger.responses.error.AccessDeniedResponses;
import org.example.what_seoul.swagger.responses.error.CommonErrorResponses;
import org.springframework.data.domain.Slice;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
@Tag(name = "Board API", description = "서울시 주요 장소의 문화행사 후기와 관련된 기능입니다.")
public class BoardController {
    private final BoardService boardService;

    @Operation(summary = "문화행사 후기 작성", description = "특정 문화행사에 대한 후기를 작성합니다. 후기는 1자 이상 300자 이하로 작성할 수 있습니다.")
    @CommonErrorResponses
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "후기 작성 성공")
    })
    @PostMapping("")
    public ResponseEntity<CommonResponse<ResCreateBoardDTO>> createBoard(@Valid @RequestBody ReqCreateBoardDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boardService.createBoard(req));
    }

    @Operation(summary = "문화행사별 후기 목록 조회", description = "문화행사 ID로 후기를 페이지 단위로 조회합니다. 최신순으로 정렬됩니다.")
    @CommonErrorResponses
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "문화행사별 후기 목록 조회 성공")
    })
    @GetMapping("")
    public ResponseEntity<CommonResponse<Slice<ResGetBoardDTO>>> getBoardsByCultureEventId(
            @RequestParam(name = "cultureEventId") Long cultureEventId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(boardService.getBoardsByCultureEventId(cultureEventId, page, size));
    }

    @Operation(summary = "후기 상세 조회", description = "후기 ID를 통해 후기 상세 정보를 조회합니다.")
    @CommonErrorResponses
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "후기 상세 조회 성공")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<ResGetBoardDTO>> getBoardById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(boardService.getBoardById(id));
    }


    @Operation(summary = "내가 작성한 후기 목록 조회", description = BoardDescription.GET_MY_BOARDS)
    @CommonErrorResponses
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작성한 후기 목록 조회 성공")
    })
    @PostMapping("/my")
    public ResponseEntity<CommonResponse<Slice<ResGetMyBoardDTO>>> getMyBoards(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "sort", defaultValue = "desc") String sort,
            @RequestBody(required = false) ReqGetMyBoardDTO req
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(boardService.getMyBoards(page, size, startDate, endDate, sort, req));
    }


    @Operation(summary = "후기 수정", description = BoardDescription.UPDATE_BOARD)
    @CommonErrorResponses
    @AccessDeniedResponses
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "후기 수정 성공")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<ResUpdateBoardDTO>> updateBoard(@PathVariable Long id, @RequestBody ReqUpdateBoardDTO req) {
        return ResponseEntity.status(HttpStatus.OK).body(boardService.updateBoard(id, req));
    }


    @Operation(summary = "후기 삭제", description = BoardDescription.DELETE_BOARD)
    @CommonErrorResponses
    @AccessDeniedResponses
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "후기 삭제 성공")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<ResDeleteBoardDTO>> deleteBoard(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(boardService.deleteBoard(id));
    }

}
