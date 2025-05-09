package org.example.what_seoul.controller.board.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.board.Board;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResUpdateBoardDTO {
    private Long id;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    private String author;
    private String eventName;

    public ResUpdateBoardDTO(Board board) {
        this.id = board.getId();
        this.content = board.getContent();
        this.createdAt = board.getCreatedAt();
        this.updatedAt = board.getUpdatedAt();
        this.author = board.getUser().getNickName();
        this.eventName = board.getCultureEvent().getEventName();
    }

    public static ResUpdateBoardDTO from(Board board) {
        return new ResUpdateBoardDTO(board);
    }
}
