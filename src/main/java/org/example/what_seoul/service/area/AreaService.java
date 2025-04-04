package org.example.what_seoul.service.area;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.area.dto.PlaceDTO;
import org.example.what_seoul.controller.area.dto.ReqLocationBasedCityDataDTO;
import org.example.what_seoul.controller.area.dto.ResGetAreaDTO;
import org.example.what_seoul.controller.area.dto.ResLocationBasedCityDataDTO;
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

    public CommonResponse<ResLocationBasedCityDataDTO> getLocationBasedCityData(ReqLocationBasedCityDataDTO reqLocationBasedCityDataDTO) {
        List<PlaceDTO> nearestPlaces = locationChecker.findLocations(reqLocationBasedCityDataDTO.getLatitude(), reqLocationBasedCityDataDTO.getLongitude());
        return new CommonResponse<>(
                true,
                "현위치 기반 도시데이터 조회 성공",
                new ResLocationBasedCityDataDTO(nearestPlaces)
        );
    }

    public CommonResponse<ResGetAreaDTO> getAreaByKeyword(String query) {
        List<Area> areaList = areaRepository.findByAreaNameContaining(query.trim()).orElseThrow(() -> new EntityNotFoundException("Area not found"));

        WKTReader wktReader = new WKTReader(new GeometryFactory());
        List<PlaceDTO> placeDTOList = new ArrayList<>();

        for (Area area : areaList) {
            try {
                Polygon polygon = (Polygon) wktReader.read(area.getPolygonWkt());
                placeDTOList.add(PlaceDTO.from(area, polygon));
            } catch (ParseException e) {

                log.error("Error parsing WKT: {}", e.getMessage());
                throw new RuntimeException("Invalid polygon data", e);
            }
        }

        return new CommonResponse<>(
                true,
                "장소 조회 성공",
                new ResGetAreaDTO(placeDTOList)
        );
    }
}
