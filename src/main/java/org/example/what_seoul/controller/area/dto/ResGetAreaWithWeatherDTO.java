package org.example.what_seoul.controller.area.dto;

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
    private Long areaId;
    private String areaName;
    private List<CoordinateDTO> polygonCoords;
    private String temperature;
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
