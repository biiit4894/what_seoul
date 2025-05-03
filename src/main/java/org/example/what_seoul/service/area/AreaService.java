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

    /**
     * 현위치 기반 장소 리스트 조회 기능
     * @param req
     * @return
     */
    public CommonResponse<ResGetAreaListByCurrentLocationDTO> getAreaListByCurrentLocation(ReqGetAreaListByCurrentLocationDTO req) {
        try {
            List<AreaDTO> nearestPlaces = locationChecker.findLocations(req.getLatitude(), req.getLongitude());
            return new CommonResponse<>(
                    true,
                    "현위치 기반 장소 리스트 조회 성공",
                    new ResGetAreaListByCurrentLocationDTO(nearestPlaces)
            );
        } catch (Exception e) {
            log.error("현위치 인근 장소 조회 실패: {}", e.getMessage());
            throw new RuntimeException("현위치 인근 장소 조회에 실패했습니다.", e);
        }
    }

    /**
     * 장소 검색 기능
     * @param query
     * @return
     */
    public CommonResponse<ResGetAreaListByKeywordDTO> getAreaListByKeyword(String query) {
        try {
            List<Area> areaList = areaRepository.findByAreaNameContaining(query.trim()).orElseThrow(() -> new EntityNotFoundException("장소 검색에 실패했습니다."));

            List<AreaDTO> areaDTOList = convertAreaDtoAreaDTOList(areaList);

            return new CommonResponse<>(
                    true,
                    "장소 검색 성공",
                    new ResGetAreaListByKeywordDTO(areaDTOList)
            );
        } catch (EntityNotFoundException e) {
            log.error("장소 검색 실패: {}", e.getMessage());
            throw new EntityNotFoundException("장소 검색에 실패했습니다.", e);
        }
    }

    /**
     * 전체 장소 리스트 조회 기능
     * @return
     */
    public CommonResponse<List<AreaDTO>> getAllAreaList() {
        List<Area> areaList = areaRepository.findAll();
        List<AreaDTO> areaDTOList = convertAreaDtoAreaDTOList(areaList);

        return new CommonResponse<>(
                true,
                "전체 장소 리스트 조회 성공",
                areaDTOList
        );
    }

    /**
     * 전체 장소 혼잡도 조회 기능
     * @return
     */
    public CommonResponse<List<ResGetAreaWithCongestionLevelDTO>> getAllAreasWithCongestionLevel() {
        List<AreaWithCongestionLevelDTO> areaList = areaRepository.findAllAreasWithCongestionLevel();

        WKTReader wktReader = new WKTReader(new GeometryFactory());
        List<ResGetAreaWithCongestionLevelDTO> areaDTOList = new ArrayList<>();

        for (AreaWithCongestionLevelDTO area : areaList) {
            try {
                Polygon polygon = (Polygon) wktReader.read(area.getPolygonWkt());
                areaDTOList.add(ResGetAreaWithCongestionLevelDTO.from(area, polygon));
            } catch (ParseException e) {

                log.error("WKT 파싱 오류: {}", e.getMessage());
                throw new RuntimeException("Invalid polygon data for area: " + area.getAreaName(), e);
            }
        }

        return new CommonResponse<>(
                true,
                "전체 장소 혼잡도 조회 성공",
                areaDTOList
        );
    }

    /**
     * 전체 장소 날씨 조회 기능
     * @return
     */
    public CommonResponse<List<ResGetAreaWithWeatherDTO>> getAllAreasWithWeather() {
        List<AreaWithWeatherDTO> areaList = areaRepository.findAllAreasWithWeather();

        WKTReader wktReader = new WKTReader(new GeometryFactory());
        List<ResGetAreaWithWeatherDTO> areaDTOList = new ArrayList<>();

        for (AreaWithWeatherDTO area : areaList) {
            try {
                Polygon polygon = (Polygon) wktReader.read(area.getPolygonWkt());
                areaDTOList.add(ResGetAreaWithWeatherDTO.from(area, polygon));
            } catch (ParseException e) {
                log.error("WKT 파싱 오류: {}", e.getMessage());
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

                log.error("WKT 파싱 오류: {}", e.getMessage());
                throw new RuntimeException("Invalid polygon data for area: " + area.getAreaName(), e);
            }
        }

        return areaDTOList;
    }


}
