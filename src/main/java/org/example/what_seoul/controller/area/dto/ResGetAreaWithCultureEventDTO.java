package org.example.what_seoul.controller.area.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.domain.citydata.event.CultureEvent;
import org.locationtech.jts.geom.Polygon;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResGetAreaWithCultureEventDTO {
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


    @Schema(description = "서울시 주요 장소의 문화행사 목록")
    private List<CultureEventDTO> cultureEventList;

    public static ResGetAreaWithCultureEventDTO from(Area area, List<CultureEvent> cultureEventsForArea, Polygon polygon) {
        List<CoordinateDTO> coordinates = Arrays
                .stream(polygon.getCoordinates())
                .map(coordinate -> new CoordinateDTO(coordinate.y, coordinate.x))
                .collect(Collectors.toList());

        List<CultureEventDTO> cultureEventDTOS = cultureEventsForArea.stream()
                .map(e -> new CultureEventDTO(
                        e.getId(),
                        e.getEventName(),
                        e.getEventPeriod(),
                        e.getEventPlace(),
                        e.getEventX(),
                        e.getEventY(),
                        e.getThumbnail(),
                        e.getUrl(),
                        e.getIsEnded()
                )).toList();

        return new ResGetAreaWithCultureEventDTO(
                area.getId(),
                area.getAreaName(),
                coordinates,
                cultureEventDTOS
        );
    }
}
