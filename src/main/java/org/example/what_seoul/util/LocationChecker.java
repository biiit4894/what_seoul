package org.example.what_seoul.util;


import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LocationChecker {
    public boolean isInsideZone(double lon, double lat, String geoJson) throws Exception {
        GeometryFactory factory = new GeometryFactory();
        Point userPoint = factory.createPoint(new Coordinate(lon, lat));

        GeoJsonReader reader = new GeoJsonReader(factory);
        Geometry geometry = reader.read(geoJson);
        return geometry.contains(userPoint);
    }
}
