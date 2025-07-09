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
        int skipped = 0;

        for (JsonNode feature : root.get("features")) {
            total++;

            String name = feature.get("properties").get("areaName").asText();
            String code = feature.get("properties").get("areaCode").asText();
            String category = feature.get("properties").get("category").asText();

            String geometry = objectMapper.writeValueAsString(feature.get("geometry"));

            String wkt = convertGeometryToWkt(geometry);

            if (areaRepository.existsByAreaName(name)) {
                skipped++;

                // TODO: 그냥 스킵이 아니라 .. updatedAt / deletedAt ???
                continue;

            }

            Area area = new Area(category, code, name);
            area.setPolygonWkt(wkt);
            areasToSave.add(area);
        }

        areaRepository.saveAll(areasToSave);
        log.info("GeoJSON에서 {}개 지역 저장 완료", areasToSave.size());

        return new ResUploadAreaDTO(total, areasToSave.size(), skipped);
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
            throw new RuntimeException(" " , e);
        }
    }
}
