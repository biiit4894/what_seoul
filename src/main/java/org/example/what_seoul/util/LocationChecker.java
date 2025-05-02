package org.example.what_seoul.util;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.controller.area.dto.AreaDTO;
import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.repository.area.AreaRepository;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * 사용자의 현재 위치 좌표를 기반으로,
 * 해당 위치를 포함하는 지역(Area)을 찾아 반환하거나,
 * 포함하는 지역이 없을 경우 가장 가까운 3개의 지역을 찾아 반환하는 클래스
 *
 * 내부적으로 JTS 라이브러리를 사용하여 WKT 문자열을 Polygon 객체로 파싱하고,
 * Geometry 연산을 통해 포함 여부 및 거리 계산을 수행한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LocationChecker {
    private final AreaRepository areaRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    /**
     * 사용자 위치와 각 지역 간의 거리 정보를 담는 내부 클래스.
     * 거리 기준으로 정렬되기 위해 PriorityQueue와 함께 사용된다.
     */
    private static class PlaceDistance {
        Long areaId;
        String placeName;
        double distance;
        Polygon polygon;

        public PlaceDistance(Long areaId, String placeName, double distance, Polygon polygon) {
            this.areaId = areaId;
            this.placeName = placeName;
            this.distance = distance;
            this.polygon = polygon;
        }
    }

    /**
     * 주어진 위도(lat)와 경도(lon)를 기준으로,
     * 해당 위치를 포함하는 지역 목록을 반환하는 메소드.
     *
     * 포함하는 지역이 없을 경우, 가장 가까운 3개의 지역을 거리 기준으로 계산하여 반환한다.
     *
     * @param lat 위도
     * @param lon 경도
     * @return 해당 위치를 포함하거나 가장 가까운 3개의 AreaDTO 목록
     */
    public List<AreaDTO> findLocations(double lat, double lon) {

        List<Area> areaList = areaRepository.findAll();
        List<AreaDTO> nearestPlaces = new ArrayList<>();
        Point userLocation = geometryFactory.createPoint(new Coordinate(lon, lat));
        WKTReader wktReader = new WKTReader(geometryFactory);

        // 유저 위치를 포함하는 폴리곤 찾기
        for (Area area : areaList) {
            try {
                Polygon polygon = (Polygon) wktReader.read(area.getPolygonWkt());
                if (polygon.contains(userLocation)) {
                    nearestPlaces.add(AreaDTO.from(area, polygon));
                }
            } catch (ParseException e) {
                log.error("Error parsing WKT: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        }

        if (!nearestPlaces.isEmpty()) {
            return nearestPlaces;
        }

        // 유저 위치를 포함(contains)하는 폴리곤이 없다면, 가장 가까운 3개의 장소 반환
        PriorityQueue<PlaceDistance> pq = new PriorityQueue<>(Comparator.comparingDouble(p -> p.distance));
        for (Area area : areaList) {
            try {
                Polygon polygon = (Polygon) wktReader.read(area.getPolygonWkt());
                double distance = polygon.distance(userLocation);
                pq.offer(new PlaceDistance(area.getId(), area.getAreaName(), distance, polygon));
            } catch (Exception e) {
                log.error("Error computing distance: {}", e.getMessage());
            }
        }

        for (int i = 0; i < 3 && !pq.isEmpty(); i++) {
            PlaceDistance nearest = pq.poll();
            nearestPlaces.add(AreaDTO.from(nearest.areaId, nearest.placeName, nearest.polygon));
        }
        return nearestPlaces;
    }
}
