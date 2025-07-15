package org.example.what_seoul.controller.board.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.board.Board;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResCreateBoardDTO {
    @Schema(description = "행사 후기 ID", example = "1")
    private Long id;

    @Schema(description = "행사 후기 내용", example = "후기 내용입니다.")
    private String content;

    @Schema(description = "행사 후기 작성 일자", example = "2025-07-15T12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "행사 후기 작성자 닉네임", example = "홍길동")
    private String author;

    @Schema(description = "행사 이름", example = "[서울생활문화센터 낙원] 좋은노래공작소 시리즈 1 [아낙동]")
    private String eventName;

    public ResCreateBoardDTO(Board board) {
        this.id = board.getId();
        this.content = board.getContent();
        this.createdAt = board.getCreatedAt();
        this.author = board.getUser().getNickName();
        this.eventName = board.getCultureEvent().getEventName();
    }

    public static ResCreateBoardDTO from(Board board) {
        return new ResCreateBoardDTO(board);
    }
}
