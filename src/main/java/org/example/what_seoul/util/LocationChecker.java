package org.example.what_seoul.util;


import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.controller.citydata.dto.CoordinateDTO;
import org.example.what_seoul.controller.citydata.dto.PlaceDTO;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

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

    private static class PlaceDistance {
        String placeName;
        double distance;
        int index;

        public PlaceDistance(String placeName, double distance, int index) {
            this.placeName = placeName;
            this.distance = distance;
            this.index = index;
        }
    }

    public List<PlaceDTO> findLocations(double lat, double lon) {
        log.info("Loaded {} polygons", polygons.size());

        List<PlaceDTO> nearestPlaces = new ArrayList<>();
        Point userLocation = geometryFactory.createPoint(new Coordinate(lon, lat));

        // 유저 위치를 포함하는 폴리곤이 있다면 해당 장소 이름만 반환
        for (int i = 0; i < polygons.size(); i++) {
            if(polygons.get(i).contains(userLocation)) {
                nearestPlaces.add(createPlaceDTO(i));
            }
        }

        if (!nearestPlaces.isEmpty()) {
            return nearestPlaces;
        }

        // 유저 위치를 포함하는 폴리곤이 없다면, 가장 가까운 3개의 장소 반환
        PriorityQueue<PlaceDistance> pq = new PriorityQueue<>(Comparator.comparingDouble(p -> p.distance));
        for (int i = 0; i < polygons.size(); i++) {
            double distance = polygons.get(i).distance(userLocation);
            pq.offer(new PlaceDistance(placeNames.get(i), distance, i));
        }

        for (int i = 0; i < 3 && !pq.isEmpty(); i++) {
            PlaceDistance nearest = pq.poll();
            nearestPlaces.add(createPlaceDTO(nearest.index));
        }

        return nearestPlaces;
    }

    private PlaceDTO createPlaceDTO(int index) {
        String areaName = placeNames.get(index);
        Polygon polygon = polygons.get(index);
        List<CoordinateDTO> coordinates = new ArrayList<>();

        Coordinate[] coords = polygon.getCoordinates();
        for (Coordinate coord : coords) {
            coordinates.add(new CoordinateDTO(coord.y, coord.x));
        }
        return new PlaceDTO(areaName, coordinates);
    }
}
