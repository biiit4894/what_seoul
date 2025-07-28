package org.example.what_seoul.controller.board.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReqCreateBoardDTO {
    @Schema(description = "후기 내용 (1~300자)", example = "후기 내용입니다.", required = true)
    @NotBlank(message = "후기를 입력해주세요.")
    @Size(min = 1, max = 300, message = "후기는 300자 이하로 작성해야 합니다.")
    private String content;

    @Schema(description = "후기를 작성하려는 문화행사의 ID", example = "1", required = true)
    @NotNull
    private Long cultureEventId;
}
