package org.example.what_seoul.controller.board.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.board.Board;
import org.example.what_seoul.domain.user.RoleType;
import org.example.what_seoul.service.user.dto.LoginUserInfoDTO;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResGetBoardDTO {
    private Long id;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    private String author;
    private String eventName;
    private boolean isEditable;

    public ResGetBoardDTO(Board board, LoginUserInfoDTO loginUserInfo) {
        this.id = board.getId();
        this.content = board.getContent();
        this.createdAt = board.getCreatedAt();
        this.updatedAt = board.getUpdatedAt();
        this.author = board.getUser().getDeletedAt() != null ? "(탈퇴한 유저)" : board.getUser().getNickName();
        this.eventName = board.getCultureEvent().getEventName();
        this.isEditable = loginUserInfo.getRole() == RoleType.ADMIN || board.getUser().getId().equals(loginUserInfo.getId());
    }

    public static ResGetBoardDTO from(Board board, LoginUserInfoDTO loginUserInfo) {
        return new ResGetBoardDTO(board, loginUserInfo);
    }
}
