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
public class ResGetAreaWithCongestionLevelDTO {
    private Long populationId;

    @Schema(description = "조회한 서울시 주요 장소의 ID", example = "33")
    private Long areaId;

    @Schema(description = "조회한 서울시 주요 장소의 장소명", example = "서울역")
    private String areaName;

    @Schema(
            description = """
            조회한 서울시 주요 장소의 좌표 목록\s
            - 다각형을 이루는 좌표들의 목록으로 이루어져 있습니다.
            - WKT 형식의 Polygon 데이터를 Coordinate 좌표 형태로 변환한 목록입니다.
            """
    )
    private List<CoordinateDTO> polygonCoords;

    @Schema(description = """
            조회한 서울시 주요 장소의 실시간 혼잡도
            - 혼잡도 유형은 '여유', '보통', '약간 붐빔', '붐빔' (혼잡도 오름차순) 으로 분류됩니다.
            """,
            example = "붐빔"
    )
    private String congestionLevel;

    public static ResGetAreaWithCongestionLevelDTO from(AreaWithCongestionLevelDTO area, Polygon polygon) {
        List<CoordinateDTO> coordinates = new ArrayList<>();
        for (Coordinate coord : polygon.getCoordinates()) {
            coordinates.add(new CoordinateDTO(coord.y, coord.x));
        }
        return new ResGetAreaWithCongestionLevelDTO(
                area.getPopulationId(),
                area.getAreaId(),
                area.getAreaName(),
                coordinates,
                area.getCongestionLevel()
        );
    }
}
