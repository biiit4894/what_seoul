package org.example.what_seoul.util;

import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

@Slf4j
public class PolygonParser {
    private static final WKTReader wktReader = new WKTReader(new GeometryFactory());


    public static Polygon parse(String polygonWkt, String areaName) {
        try {
            return (Polygon) wktReader.read(polygonWkt);
        } catch (ParseException e) {
            log.error("WKT 파싱 오류: {}", e.getMessage());
            throw new RuntimeException("Invalid polygon data for area: " + areaName, e);

        }
    }
}
