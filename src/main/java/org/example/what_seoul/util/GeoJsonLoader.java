package org.example.what_seoul.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Data
//@Component
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
            // GeoJson 파일 읽기
            InputStream inputStream = new ClassPathResource("data/seoul_zones.geojson").getInputStream();
            String geoJson = new Scanner(inputStream, StandardCharsets.UTF_8).useDelimiter("\\A").next();

//            log.info("GeoJSON data loaded: {}", geoJson);

            // JSON 파싱
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(geoJson);
            GeoJsonReader reader = new GeoJsonReader();

            // "features" 배열 얻기
            JSONArray features = (JSONArray) jsonObject.get("features");
//            log.info("features size: {}", features.size());

            // "features" 배열 순회하여 장소 이름 접근
            for (int i = 0; i < features.size(); i++) {
                JSONObject feature = (JSONObject) features.get(i);
                JSONObject properties = (JSONObject) feature.get("properties");
                String areaName = properties.get("AREA_NM").toString();

                JSONObject geometryJson = (JSONObject) feature.get("geometry");
                Geometry geometry = reader.read(geometryJson.toString());
//                log.info("Geometry type: {}", geometry.getGeometryType()); // Polygon

                if (geometry instanceof Polygon) {
                    polygons.add((Polygon) geometry);
                    placeNames.add(areaName);
                }

            }

        } catch (Exception e) {
            log.error("{}", e.getMessage());
        }
    }

}
