package org.example.what_seoul.controller.area.dto;

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
    private Long id;
    private String areaName;
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
