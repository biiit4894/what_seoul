package org.example.what_seoul.controller.area.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResGetAreaListByKeywordDTO {
    @Schema(description = "검색 결과 조회된 서울시 주요 장소 목록")
    private List<AreaDTO> areaList;
}
