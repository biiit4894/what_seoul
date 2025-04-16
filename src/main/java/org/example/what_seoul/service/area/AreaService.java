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

    public CommonResponse<ResGetAreaListByCurrentLocationDTO> getLocationBasedCityData(ReqGetAreaListByCurrentLocationDTO reqGetAreaListByCurrentLocationDTO) {
        List<AreaDTO> nearestPlaces = locationChecker.findLocations(reqGetAreaListByCurrentLocationDTO.getLatitude(), reqGetAreaListByCurrentLocationDTO.getLongitude());
        return new CommonResponse<>(
                true,
                "현위치 기반 도시데이터 조회 성공",
                new ResGetAreaListByCurrentLocationDTO(nearestPlaces)
        );
    }

    public CommonResponse<ResGetAreaListByKeywordDTO> getAreaListByKeyword(String query) {
        List<Area> areaList = areaRepository.findByAreaNameContaining(query.trim()).orElseThrow(() -> new EntityNotFoundException("Area not found"));

        WKTReader wktReader = new WKTReader(new GeometryFactory());
        List<AreaDTO> areaDTOList = new ArrayList<>();

        for (Area area : areaList) {
            try {
                Polygon polygon = (Polygon) wktReader.read(area.getPolygonWkt());
                areaDTOList.add(AreaDTO.from(area, polygon));
            } catch (ParseException e) {

                log.error("Error parsing WKT: {}", e.getMessage());
                throw new RuntimeException("Invalid polygon data", e);
            }
        }

        return new CommonResponse<>(
                true,
                "장소 조회 성공",
                new ResGetAreaListByKeywordDTO(areaDTOList)
        );
    }

    public CommonResponse<List<AreaDTO>> getAllAreaList() {
        List<Area> areaList = areaRepository.findAll();

        WKTReader wktReader = new WKTReader(new GeometryFactory());
        List<AreaDTO> areaDTOList = new ArrayList<>();

        for (Area area : areaList) {
            try {
                Polygon polygon = (Polygon) wktReader.read(area.getPolygonWkt());
                areaDTOList.add(AreaDTO.from(area, polygon));
            } catch (ParseException e) {

                log.error("Error parsing WKT: {}", e.getMessage());
                throw new RuntimeException("Invalid polygon data", e);
            }
        }

        return new CommonResponse<>(
                true,
                "장소 전체 조회 성공",
                areaDTOList
        );
    }

    // TODO: ResAreaWithCongestionLevelDTO를 repository에서 조회해와서, polygonWkt를 polygonCoords로 변환 필요
    // TODO: ResAreaWithCongestionLevelDTO의 DTO 네이밍 변경 + 새로운 응답 DTO 필요
    public CommonResponse<List<ResGetAreaWithCongestionLevelDTO>> getAllAreasWithCongestionLevel() {
//        List<Area> areaList = areaRepository.findAll();
        List<AreaWithCongestionLevelDTO> areaList = areaRepository.findAllAreasWithCongestionLevel();

        WKTReader wktReader = new WKTReader(new GeometryFactory());
        List<ResGetAreaWithCongestionLevelDTO> areaDTOList = new ArrayList<>();

        for (AreaWithCongestionLevelDTO area : areaList) {
            try {
                Polygon polygon = (Polygon) wktReader.read(area.getPolygonWkt());
                areaDTOList.add(ResGetAreaWithCongestionLevelDTO.from(area, polygon));
            } catch (ParseException e) {

                log.error("Error parsing WKT: {}", e.getMessage());
                throw new RuntimeException("Invalid polygon data", e);
            }
        }

        return new CommonResponse<>(
                true,
                "장소 전체 조회 성공",
                areaDTOList
        );
    }

}
