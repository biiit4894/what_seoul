package org.example.what_seoul.controller.area.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResGetAreaWithWeatherDTO {
    private Long weatherId;

    @Schema(description = "서울시 주요 장소의 ID", example = "33")
    private Long areaId;

    @Schema(description = "서울시 주요 장소의 장소명", example = "서울역")
    private String areaName;

    @Schema(
            description = """
            조회한 서울시 주요 장소의 좌표 목록\s
            - 다각형을 이루는 좌표들의 목록으로 이루어져 있습니다.
            - WKT 형식의 Polygon 데이터를 Coordinate 좌표 형태로 변환한 목록입니다.
            """
    )
    private List<CoordinateDTO> polygonCoords;

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

    public static ResGetAreaWithWeatherDTO from(AreaWithWeatherDTO area, Polygon polygon) {
        List<CoordinateDTO> coordinates = new ArrayList<>();
        for (Coordinate coord : polygon.getCoordinates()) {
            coordinates.add(new CoordinateDTO(coord.y, coord.x));
        }
        return new ResGetAreaWithWeatherDTO(
                area.getWeatherId(),
                area.getAreaId(),
                area.getAreaName(),
                coordinates,
                area.getTemperature(),
                area.getPcpMsg()
        );
    }
}
