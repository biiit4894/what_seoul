package org.example.what_seoul.controller.area.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AreaWithCongestionLevelDTO {
    @Schema(description = "서울시 주요 장소의 실시간 인구 현황 정보 ID", example = "694736")
    private Long populationId;

    @Schema(description = "서울시 주요 장소의 ID", example = "33")
    private Long areaId;

    @Schema(description = "서울시 주요 장소의 장소명", example = "서울역")
    private String areaName;

    @Schema(description = "서울시 주요 장소의 Polygon 데이터 (WKT 형식)", example = "POLYGON ((126.972413 37.55465, 126.972412 37.554652, ... , 126.972413 37.55465))")
    private String polygonWkt;

    @Schema(description = "서울시 주요 장소의 실시간 인구 혼잡도", example = "붐빔")
    private String congestionLevel;
}
