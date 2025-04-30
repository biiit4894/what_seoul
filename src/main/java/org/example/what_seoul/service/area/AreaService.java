package org.example.what_seoul.service.area;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.area.dto.*;
import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.repository.area.AreaRepository;
import org.example.what_seoul.util.LocationChecker;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AreaService {
    private final AreaRepository areaRepository;
    private final LocationChecker locationChecker;

    public CommonResponse<ResGetAreaListByCurrentLocationDTO> getAreaListByCurrentLocation(ReqGetAreaListByCurrentLocationDTO req) {
        try {
            List<AreaDTO> nearestPlaces = locationChecker.findLocations(req.getLatitude(), req.getLongitude());
            return new CommonResponse<>(
                    true,
                    "현위치 기반 장소 리스트 조회 성공",
                    new ResGetAreaListByCurrentLocationDTO(nearestPlaces)
            );
        } catch (Exception e) {
            log.error("Error finding nearest locations: {}", e.getMessage());
            throw new RuntimeException("Failed to find nearby areas", e);
        }
    }

    public CommonResponse<ResGetAreaListByKeywordDTO> getAreaListByKeyword(String query) {
        List<Area> areaList = areaRepository.findByAreaNameContaining(query.trim()).orElseThrow(() -> new EntityNotFoundException("Area not found"));

        List<AreaDTO> areaDTOList = convertAreaDtoAreaDTOList(areaList);

        return new CommonResponse<>(
                true,
                "장소 검색 성공",
                new ResGetAreaListByKeywordDTO(areaDTOList)
        );
    }

    public CommonResponse<List<AreaDTO>> getAllAreaList() {
        List<Area> areaList = areaRepository.findAll();
        List<AreaDTO> areaDTOList = convertAreaDtoAreaDTOList(areaList);

        return new CommonResponse<>(
                true,
                "전체 장소 리스트 조회 성공",
                areaDTOList
        );
    }



    public CommonResponse<List<ResGetAreaWithCongestionLevelDTO>> getAllAreasWithCongestionLevel() {
        List<AreaWithCongestionLevelDTO> areaList = areaRepository.findAllAreasWithCongestionLevel();

        WKTReader wktReader = new WKTReader(new GeometryFactory());
        List<ResGetAreaWithCongestionLevelDTO> areaDTOList = new ArrayList<>();

        for (AreaWithCongestionLevelDTO area : areaList) {
            try {
                Polygon polygon = (Polygon) wktReader.read(area.getPolygonWkt());
                areaDTOList.add(ResGetAreaWithCongestionLevelDTO.from(area, polygon));
            } catch (ParseException e) {

                log.error("Error parsing WKT: {}", e.getMessage());
                throw new RuntimeException("Invalid polygon data for area: " + area.getAreaName(), e);
            }
        }

        return new CommonResponse<>(
                true,
                "전체 장소 혼잡도 조회 성공",
                areaDTOList
        );
    }

    public CommonResponse<List<ResGetAreaWithWeatherDTO>> getAllAreasWithWeather() {
        List<AreaWithWeatherDTO> areaList = areaRepository.findAllAreasWithWeather();

        WKTReader wktReader = new WKTReader(new GeometryFactory());
        List<ResGetAreaWithWeatherDTO> areaDTOList = new ArrayList<>();

        for (AreaWithWeatherDTO area : areaList) {
            try {
                Polygon polygon = (Polygon) wktReader.read(area.getPolygonWkt());
                areaDTOList.add(ResGetAreaWithWeatherDTO.from(area, polygon));
            } catch (ParseException e) {
                log.error("Error parsing WKT: {}", e.getMessage());
                throw new RuntimeException("Invalid polygon data for area: " + area.getAreaName(), e);

            }
        }

        return new CommonResponse<>(
                true,
                "전체 장소 날씨 조회 성공",
                areaDTOList
        );
    }

    private List<AreaDTO> convertAreaDtoAreaDTOList(List<Area> areaList) {
        WKTReader wktReader = new WKTReader(new GeometryFactory());
        List<AreaDTO> areaDTOList = new ArrayList<>();

        for (Area area : areaList) {
            try {
                Polygon polygon = (Polygon) wktReader.read(area.getPolygonWkt());
                areaDTOList.add(AreaDTO.from(area, polygon));
            } catch (ParseException e) {

                log.error("Error parsing WKT: {}", e.getMessage());
                throw new RuntimeException("Invalid polygon data for area: " + area.getAreaName(), e);
            }
        }

        return areaDTOList;
    }


}
