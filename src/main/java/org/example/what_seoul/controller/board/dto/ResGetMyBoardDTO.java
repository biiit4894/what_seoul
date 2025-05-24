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
public class ResGetMyBoardDTO {
    private Long id;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    private String eventName;
    private String eventPlace;
    private String areaName;
    private boolean isEnded;

    public ResGetMyBoardDTO(Board board) {
        this.id = board.getId();
        this.content = board.getContent();
        this.createdAt = board.getCreatedAt();
        this.updatedAt = board.getUpdatedAt();
        this.eventName = board.getCultureEvent().getEventName();
        this.eventPlace = board.getCultureEvent().getEventPlace();
        this.areaName = board.getCultureEvent().getArea().getAreaName();
        this.isEnded = board.getCultureEvent().getIsEnded();
    }

    public static ResGetMyBoardDTO from(Board board) {
        return new ResGetMyBoardDTO(board);
    }
}
