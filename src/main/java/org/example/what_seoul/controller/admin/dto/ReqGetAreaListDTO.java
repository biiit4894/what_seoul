package org.example.what_seoul.controller.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReqGetAreaListDTO {
    @Schema(description = "서울시 주요 장소 검색어", example = "서울역")
    String areaName;
}
