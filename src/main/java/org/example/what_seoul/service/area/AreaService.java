package org.example.what_seoul.service.area;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.admin.dto.ResDeleteAreaDTO;
import org.example.what_seoul.controller.area.dto.*;
import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.domain.citydata.event.CultureEvent;
import org.example.what_seoul.repository.area.AreaRepository;
import org.example.what_seoul.repository.citydata.event.CultureEventRepository;
import org.example.what_seoul.service.user.UserService;
import org.example.what_seoul.service.user.dto.LoginUserInfoDTO;
import org.example.what_seoul.util.LocationChecker;
import org.example.what_seoul.util.PolygonParser;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AreaService {
    private final AreaRepository areaRepository;
    private final CultureEventRepository cultureEventRepository;
    private final UserService userService;
    private final LocationChecker locationChecker;

    /**
     * 현위치 기반 장소 리스트 조회 기능
     * @param req
     * @return
     */
    @Transactional(readOnly = true)
    public CommonResponse<ResGetAreaListByCurrentLocationDTO> getAreaListByCurrentLocation(ReqGetAreaListByCurrentLocationDTO req) {
        List<AreaDTO> nearestPlaces = locationChecker.findLocations(req.getLatitude(), req.getLongitude());
        return new CommonResponse<>(
                true,
                "현위치 기반 장소 리스트 조회 성공",
                new ResGetAreaListByCurrentLocationDTO(nearestPlaces)
        );
    }

    /**
     * 장소 검색 기능
     * - 삭제 처리되지 않은 유효한 서울시 주요 장소에 한하여 검색한다
     * @param query
     * @return
     */
    @Transactional(readOnly = true)
    public CommonResponse<ResGetAreaListByKeywordDTO> getAreaListByKeyword(String query) {
        List<Area> areaList = areaRepository.findByAreaNameContainingAndDeletedAtIsNull(query.trim());

        List<AreaDTO> areaDTOList = convertAreaDtoAreaDTOList(areaList);

        return new CommonResponse<>(
                true,
                "장소 검색 성공",
                new ResGetAreaListByKeywordDTO(areaDTOList)
        );
    }

    /**
     * 전체 장소 리스트 조회 기능
     * - 삭제 처리되지 않은 유효한 서울시 주요 장소 리스트만을 조회한다
     * @return
     */
    @Transactional(readOnly = true)
    public CommonResponse<List<AreaDTO>> getAllAreaList() {
        List<Area> areaList = areaRepository.findByDeletedAtIsNull();
        List<AreaDTO> areaDTOList = convertAreaDtoAreaDTOList(areaList);

        return new CommonResponse<>(
                true,
                "전체 장소 리스트 조회 성공",
                areaDTOList
        );
    }

    /**
     * 전체 장소 혼잡도 조회 기능
     * - 삭제 처리되지 않은 전체 장소에 한해 조회한다.
     * @return
     */
    @Transactional(readOnly = true)
    public CommonResponse<List<ResGetAreaWithCongestionLevelDTO>> getAllAreasWithCongestionLevel() {
        List<AreaWithCongestionLevelDTO> areaList = areaRepository.findAllAreasWithCongestionLevel();

        List<ResGetAreaWithCongestionLevelDTO> areaDTOList = new ArrayList<>();

        for (AreaWithCongestionLevelDTO area : areaList) {
            Polygon polygon = PolygonParser.parse(area.getPolygonWkt(), area.getAreaName());
            areaDTOList.add(ResGetAreaWithCongestionLevelDTO.from(area, polygon));
        }

        return new CommonResponse<>(
                true,
                "전체 장소 혼잡도 조회 성공",
                areaDTOList
        );
    }

    /**
     * 전체 장소 날씨 조회 기능
     * - 삭제 처리되지 않은 전체 장소에 한해 조회한다.
     * @return
     */
    @Transactional(readOnly = true)
    public CommonResponse<List<ResGetAreaWithWeatherDTO>> getAllAreasWithWeather() {
        List<AreaWithWeatherDTO> areaList = areaRepository.findAllAreasWithWeather();

        List<ResGetAreaWithWeatherDTO> areaDTOList = new ArrayList<>();

        for (AreaWithWeatherDTO area : areaList) {
            Polygon polygon = PolygonParser.parse(area.getPolygonWkt(), area.getAreaName());
            areaDTOList.add(ResGetAreaWithWeatherDTO.from(area, polygon));
        }

        return new CommonResponse<>(
                true,
                "전체 장소 날씨 조회 성공",
                areaDTOList
        );
    }

    /**
     * 전체 장소 문화행사 조회 기능
     * - 삭제 처리되지 않은 전체 장소에 한해 조회한다.
     * @return
     */
    @Transactional(readOnly = true)
    public CommonResponse<List<ResGetAreaWithCultureEventDTO>> getAllAreasWithCultureEvent() {
        List<Area> allAreas = areaRepository.findByDeletedAtIsNull();
        List<CultureEvent> allCultureEvents = cultureEventRepository.findAllWithArea();

        Map<Long, List<CultureEvent>> eventMap = allCultureEvents.stream()
                .filter(event -> event.getArea() != null && event.getArea().getId() != null) // null 필터링
                .collect(Collectors.groupingBy(event -> event.getArea().getId()));

        List<ResGetAreaWithCultureEventDTO> result = allAreas.stream()
                .map(area -> {
                    Polygon polygon = PolygonParser.parse(area.getPolygonWkt(), area.getAreaName());
                    List<CultureEvent> cultureEventsForArea = eventMap.getOrDefault(area.getId(), Collections.emptyList());
                    return ResGetAreaWithCultureEventDTO.from(area, cultureEventsForArea, polygon);
                }).toList();

        return new CommonResponse<>(
                true,
                "전체 장소 문화행사 조회 성공",
                result
        );
    }

    /**
     * 문화행사 후기를 작성한 장소들의 장소명 목록 조회 기능
     * @return
     */
    @Transactional(readOnly = true)
    public CommonResponse<List<String>> getAreaNamesWithMyBoards() {
        LoginUserInfoDTO loginUserInfo = userService.getLoginUserInfo();
        List<String> areaNames = areaRepository.findAreaNamesByUserId(loginUserInfo.getId());
        return new CommonResponse<>(true, "후기를 작성한 장소 이름 목록 조회 성공", areaNames);
    }

    /**
     * 장소 도메인 객체를 DTO 형태로 반환하는 메소드
     * @param areaList
     * @return
     */
    private List<AreaDTO> convertAreaDtoAreaDTOList(List<Area> areaList) {
        List<AreaDTO> areaDTOList = new ArrayList<>();

        for (Area area : areaList) {
            Polygon polygon = PolygonParser.parse(area.getPolygonWkt(), area.getAreaName());
            areaDTOList.add(AreaDTO.from(area, polygon));
        }

        return areaDTOList;
    }
}
