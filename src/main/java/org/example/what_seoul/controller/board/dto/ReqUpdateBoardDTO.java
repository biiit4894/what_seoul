package org.example.what_seoul.controller.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReqUpdateBoardDTO {
    @NotBlank(message = "후기를 입력해주세요.")
    @Size(min = 1, max = 300, message = "후기는 300자 이하로 작성해야 합니다.")
    private String content;
}
