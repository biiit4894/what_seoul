package org.example.what_seoul.controller.board.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "행사 후기 ID", example = "1")
    private Long id;

    @Schema(description = "행사 후기 내용", example = "후기 내용입니다.")
    private String content;

    @Schema(description = "행사 후기 작성 일자", example = "2025-07-15T12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "행사 후기 수정 일자 (null 허용)", example = "2025-07-15T13:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "행사 후기 작성자 닉네임", example = "홍길동")
    private String author;

    @Schema(description = "행사 이름", example = "[서울생활문화센터 낙원] 좋은노래공작소 시리즈 1 [아낙동]")
    private String eventName;

    @Schema(description = """
            행사 후기 수정 가능 여부(true/false)
            - 일반 유저 : 본인이 작성한 글만 수정 가능합니다.
            - 관리자 : 모든 글을 수정 가능합니다.
            """
            , example = "true"
    )
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
