package org.example.what_seoul.controller.area.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AreaWithWeatherDTO {
    @Schema(description = "서울시 주요 장소의 실시간 날씨 현황 정보 ID", example = "694259")
    private Long weatherId;

    @Schema(description = "서울시 주요 장소의 ID", example = "33")
    private Long areaId;

    @Schema(description = "서울시 주요 장소의 장소명", example = "서울역")
    private String areaName;

    @Schema(description = "서울시 주요 장소의 Polygon 데이터 (WKT 형식)", example = "POLYGON ((126.972413 37.55465, 126.972412 37.554652, ... , 126.972413 37.55465))")
    private String polygonWkt;

    @Schema(description = "서울시 주요 장소의 실시간 기온", example = "24.1")
    private String temperature;

    @Schema(description = """
            서울시 주요 장소의 실시간 강수량 메시지\s
            - 메시지 유형은 아래와 같습니다. (강수량 오름차순)\s
              - 비 또는 눈 소식이 없어요.\s
              - 약한 비가 내리고 있어요.외출 시 우산을 챙기세요.\s
              - 비가 내리고 있어요.외출 시 우산을 챙기세요.\s
              - 강한 비가 내리고 있어요.외출 시 우산을 챙기세요.보행 및 교통 안전에 유의해주세요.\s
              - 매우 강한 비가 내리고 있어요.외출 시 우산을 챙기세요.보행 및 교통 안전에 각별히 유의해주세요.
            """,
            example = "약한 비가 내리고 있어요.외출 시 우산을 챙기세요."
    )
    private String pcpMsg;
}
