package org.example.what_seoul.controller.area.dto;

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
    private Long areaId;
    private String areaName;
    private List<CoordinateDTO> polygonCoords;
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
