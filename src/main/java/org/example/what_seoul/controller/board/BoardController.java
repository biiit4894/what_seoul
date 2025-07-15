package org.example.what_seoul.controller.board;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.what_seoul.common.dto.CommonErrorResponse;
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
@RequiredArgsConstructor
@Tag(name = "Board API", description = "서울시 주요 장소의 문화행사 후기와 관련된 기능입니다.")
public class BoardController {
    private final BoardService boardService;

    @Operation(
            summary = "문화행사 후기 작성",
            description = "유효한 후기 내용을 포함해 특정 문화행사에 대한 후기를 작성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "후기 작성 성공",
                    content = @Content(schema = @Schema(implementation = ResCreateBoardDTO.class))),
//            @ApiResponse(responseCode = "400", description = "유효성 검사 실패", content = @Content(schema = @Schema(implementation = CommonErrorResponse.class))),
//            @ApiResponse(responseCode = "404", description = "문화행사 미존재", content = @Content(schema = @Schema(implementation = CommonErrorResponse.class)))
    })
    @PostMapping("")
    public ResponseEntity<CommonResponse<ResCreateBoardDTO>> createBoard(@Valid @RequestBody ReqCreateBoardDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boardService.createBoard(req));
    }


    @Operation(
            summary = "특정 문화행사 후기 목록 조회 (슬라이스 페이징)",
            description = "문화행사 ID로 후기를 페이지 단위로 조회합니다. 최신순으로 정렬됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "후기 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ResGetBoardDTO.class))),
//            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @GetMapping("")
    public ResponseEntity<CommonResponse<Slice<ResGetBoardDTO>>> getBoardsByCultureEventId(
            @RequestParam(name = "cultureEventId") Long cultureEventId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(boardService.getBoardsByCultureEventId(cultureEventId, page, size));
    }


    @Operation(
            summary = "후기 상세 조회",
            description = "후기 ID를 통해 후기 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "후기 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = ResGetBoardDTO.class))),
//            @ApiResponse(responseCode = "404", description = "후기 미존재")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<ResGetBoardDTO>> getBoardById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(boardService.getBoardById(id));
    }


    @Operation(
            summary = "내가 작성한 후기 목록 조회",
            description = """
            로그인한 사용자가 작성한 문화행사 후기 목록을 조회합니다. \s
            - 장소 필터링, 작성일자 필터링이 가능합니다. \s
            - 정렬 기준은 작성일 기준 오름차순/내림차순을 선택할 수 있습니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(
                        examples = @ExampleObject(value = """
                        {
                          "success": true,
                          "message": "작성한 문화행사 후기 목록 조회 성공",
                          "data": {
                            "content": [
                              {
                                "id": 1,
                                "content": "좋은 행사였어요!",
                                "createdAt": "2025-07-01T10:00:00",
                                "updatedAt": "2025-07-01T11:00:00",
                                "eventName": "2025 서울 재즈 페스티벌",
                                "eventPlace": "서울숲",
                                "url": "https://event-url.com",
                                "areaName": "성수동",
                                "areaDeletedAt": null,
                                "isEnded": true
                              }
                            ],
                            "pageable": { ... },
                            "hasNext": false
                          },
                          "responseTime": "2025-07-15T12:00:00.000Z"
                        }
                        """)
                ))
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


    @Operation(
            summary = "후기 수정",
            description = """
            특정 문화행사 후기를 수정합니다. \s
            - 일반 유저는 본인이 작성한 후기만 수정할 수 있습니다. \s
            - 내용이 기존과 동일하면 수정할 수 없습니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(
                    schema = @Schema(implementation = ResUpdateBoardDTO.class),
                    examples = @ExampleObject(value = """
        {
          "success": true,
          "message": "문화행사 후기 수정 성공",
          "data": {
            "id": 1,
            "content": "행사 정말 좋았어요!",
            "createdAt": "2025-07-01T10:00:00",
            "updatedAt": "2025-07-15T12:05:00",
            "author": "홍길동",
            "eventName": "2025 서울 재즈 페스티벌"
          },
          "responseTime": "2025-07-15T12:05:00.000Z"
        }
        """)
            )),
//            @ApiResponse(responseCode = "400", description = "유효성 검사 실패 또는 기존과 동일한 내용"),
//            @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
//            @ApiResponse(responseCode = "404", description = "후기 ID를 찾을 수 없음")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<ResUpdateBoardDTO>> updateBoard(@PathVariable Long id, @RequestBody ReqUpdateBoardDTO req) {
        return ResponseEntity.status(HttpStatus.OK).body(boardService.updateBoard(id, req));
    }


    @Operation(
            summary = "후기 삭제",
            description = """
            특정 문화행사 후기를 삭제합니다. \s
            - 일반 유저는 본인이 작성한 후기만 삭제할 수 있습니다.
            """
            )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공", content = @Content(
                    schema = @Schema(implementation = ResDeleteBoardDTO.class),
                    examples = @ExampleObject(value = """
                    {
                      "success": true,
                      "message": "문화행사 후기 삭제 성공",
                      "data": {
                        "id": 1,
                        "content": "좋은 행사였어요!",
                        "createdAt": "2025-07-01T10:00:00",
                        "updatedAt": "2025-07-01T11:00:00",
                        "author": "홍길동",
                        "eventName": "2025 서울 재즈 페스티벌"
                      },
                      "responseTime": "2025-07-15T12:10:00.000Z"
                    }
                    """)
            )),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "후기 ID를 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<ResDeleteBoardDTO>> deleteBoard(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(boardService.deleteBoard(id));
    }

}
