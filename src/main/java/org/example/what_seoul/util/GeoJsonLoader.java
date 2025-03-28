package org.example.what_seoul.util;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Data
@Component
@Slf4j
public class GeoJsonLoader {
    private final List<Polygon> polygons = new ArrayList<>();
    private final List<String> placeNames = new ArrayList<>();
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public GeoJsonLoader() {
        loadGeoJson();
    }

    private void loadGeoJson() {
        try {
            InputStream inputStream = new ClassPathResource("data/seoul_zones.geojson").getInputStream();
            String geoJson = new Scanner(inputStream, StandardCharsets.UTF_8).useDelimiter("\\A").next();

            log.info("GeoJSON data loaded: {}", geoJson);  // 추가한 디버깅 로그

            GeoJsonReader reader = new GeoJsonReader();
            Geometry geometry = reader.read(geoJson);

            log.info("Geometry type: {}", geometry.getGeometryType());  // 추가한 디버깅 로그

            if (!geometry.isEmpty()) {
                log.info("geometry is not empty");
            }

            if (geometry.getGeometryType().equals("GeometryCollection")) {
                log.info("geometryCollection");
                for (int i = 0; i < geometry.getNumGeometries(); i++) {
                    polygons.add((Polygon) geometry.getGeometryN(i));
                    placeNames.add("Place " + (i + 1));
                }
            }
        } catch (Exception e) {
            log.error("{}", e.getMessage());
        }
    }

}
