package org.example.what_seoul.controller.board.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReqGetMyBoardDTO {
    @Schema(description = "조회하고자 하는 장소 이름 목록", example = "인사동")
    List<String> selectedAreaNames;
}
