package org.example.what_seoul.controller.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.board.Board;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResCreateBoardDTO {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private String author;
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
