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
public class ResGetAreaWithCongestionLevelDTO {
    private Long populationId;
    private Long areaId;
    private String areaName;
    private List<CoordinateDTO> polygonCoords;
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
