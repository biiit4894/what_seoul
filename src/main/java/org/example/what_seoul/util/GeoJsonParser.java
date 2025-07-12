package org.example.what_seoul.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.repository.area.AreaRepository;
import org.example.what_seoul.controller.admin.dto.ResUploadAreaDTO;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeoJsonParser {

    private final ObjectMapper objectMapper;
    private final AreaRepository areaRepository;

    public ResUploadAreaDTO extractAreasFromGeoJsonAndSave(File geojsonFile) throws IOException, ParseException {
        JsonNode root = objectMapper.readTree(geojsonFile);

        if (!root.has("features")) {
            throw new IllegalArgumentException("잘못된 GeoJSON 형식입니다.");
        }

        List<Area> areasToSave = new ArrayList<>();
        int total = 0;
        int inserted = 0;
        int updated = 0;
        int skipped = 0;

        for (JsonNode feature : root.get("features")) {
            total++;

            String name = feature.get("properties").get("areaName").asText();
            String code = feature.get("properties").get("areaCode").asText();
            String category = feature.get("properties").get("category").asText();

            String geometry = objectMapper.writeValueAsString(feature.get("geometry"));
            String wkt = convertGeometryToWkt(geometry);

            Optional<Area> existingArea = areaRepository.findByAreaCode(code);

            if (existingArea.isPresent()) {
                Area area = existingArea.get();
                boolean needsUpdate = false;

                if (!area.getAreaName().equals(name)) {
                    area.setAreaName(name);
                    needsUpdate = true;
                }

                if (!area.getCategory().equals(category)) {
                    area.setCategory(category);
                    needsUpdate = true;
                }

                if (!area.getPolygonWkt().equals(wkt)) {
                    area.setPolygonWkt(wkt);
                    needsUpdate = true;

                }

                if (needsUpdate) {
                    area.setUpdatedAt();
                    updated++;
                } else {
                    skipped++;
                }
            } else {
                Area newArea = new Area(category, code, name, wkt);
                areasToSave.add(newArea);
                inserted++;
            }

        }

        if (!areasToSave.isEmpty()) {
            areaRepository.saveAll(areasToSave);
        }

        log.info("GeoJSON 처리 결과: total={}, inserted={}, updated={}, skipped={}", total, inserted, updated, skipped);

        return new ResUploadAreaDTO(total, inserted, skipped, updated);
    }

    // geometry 피쳐의 값을 wkt 형식으로 변환
    private String convertGeometryToWkt(String geoJsonString)  {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode geometryNode = mapper.readTree(geoJsonString);

            GeoJsonReader reader = new GeoJsonReader();
            Geometry geometry = reader.read(geometryNode.toString());

            WKTWriter writer = new WKTWriter();
            return writer.write(geometry);
        } catch (ParseException | JsonProcessingException e) {
            log.error("WKT 파싱 오류: {}", e.getMessage());
            throw new RuntimeException("WKT 파싱 오류" , e);
        }
    }
}
