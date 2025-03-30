package org.example.what_seoul.util;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class LocationChecker {
    private final List<Polygon> polygons;
    private final List<String> placeNames;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public LocationChecker(GeoJsonLoader geoJsonLoader) {
        this.polygons = geoJsonLoader.getPolygons();
        this.placeNames = geoJsonLoader.getPlaceNames();
    }

    public String findLocation(double lat, double lon) {
        log.info("Loaded {} polygons", polygons.size());

        Point userLocation = geometryFactory.createPoint(new Coordinate(lon, lat));

        for (int i = 0; i < polygons.size(); i++) {
            Polygon polygon = polygons.get(i);
            if(polygons.get(i).contains(userLocation)) {
                return placeNames.get(i);
            }
        }
        return "해당 없음";
    }
}
