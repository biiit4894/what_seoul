package org.example.what_seoul.controller.area.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.citydata.Area;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AreaDTO {
    @Schema(description = "조회한 서울시 주요 장소의 ID", example = "56")
    private Long id;

    @Schema(description = "조회한 서울시 주요 장소의 장소명", example = "회기역")
    private String areaName;

//    @Schema(
//            description = """
//            조회한 서울시 주요 장소의 좌표 목록\s
//            - 다각형을 이루는 좌표들의 목록으로 이루어져 있습니다.
//            - WKT 형식의 Polygon 데이터를 Coordinate 좌표 형태로 변환한 목록입니다.
//            """,
//            example = """
//            { "lat": 37.590266, "lon": 127.054142 },\s
//            ... \s
//            { "lat": 37.5903, "lon": 127.0542 }
//            """
//    )
    @Schema(
            description = """
            조회한 서울시 주요 장소의 좌표 목록
            - 다각형을 이루는 좌표들의 목록입니다.
            - WKT 형식의 Polygon 데이터를 Coordinate 좌표 형태로 변환한 리스트입니다.
            """,
            example = """
            [
              {"lat": 37.590266389129916, "lon": 127.05414215214367},
              {"lat": 37.59086715967881, "lon": 127.05243020495136},
              {"lat": 37.590566472958585, "lon": 127.052230306242}
            ]
            """
    )
    private List<CoordinateDTO> polygonCoords;

    public static AreaDTO from(Area area, Polygon polygon) {
        return from(area.getId(), area.getAreaName(), polygon);
    }
    public static AreaDTO from(Long id, String areaName, Polygon polygon) {
        List<CoordinateDTO> coordinates = new ArrayList<>();
        for (Coordinate coord : polygon.getCoordinates()) {
            coordinates.add(new CoordinateDTO(coord.y, coord.x));
        }
        return new AreaDTO(id, areaName, coordinates);
    }
}
