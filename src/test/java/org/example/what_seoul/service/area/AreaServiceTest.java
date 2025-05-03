package org.example.what_seoul.service.area;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.area.dto.*;
import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.repository.area.AreaRepository;
import org.example.what_seoul.util.LocationChecker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class AreaServiceTest {
    @InjectMocks
    private AreaService areaService;

    @Mock
    private AreaRepository areaRepository;

    @Mock
    private LocationChecker locationChecker;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AreaServiceTest() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("[성공] 현위치 기반 장소 리스트 조회 Service")
    void getAreaListByCurrentLocation() throws JsonProcessingException {
        // Given
        ReqGetAreaListByCurrentLocationDTO request = new ReqGetAreaListByCurrentLocationDTO(37.0, 127.0);
        List<AreaDTO> mockLocations = List.of(
                new AreaDTO(1L, "A", null),
                new AreaDTO(2L, "B", null)
        );

        given(locationChecker.findLocations(anyDouble(), anyDouble()))
                .willReturn(mockLocations);

        // When
        CommonResponse<ResGetAreaListByCurrentLocationDTO> response = areaService.getAreaListByCurrentLocation(request);

        // Then
        assertTrue(response.isSuccess());
        assertEquals("현위치 기반 장소 리스트 조회 성공", response.getMessage());
        assertEquals(2, response.getData().getNearestPlaces().size());

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(json);
    }

    @Test
    @DisplayName("[성공] 장소 검색 Service")
    void getAreaListByKeyword() throws JsonProcessingException {
        // Given
        String query = "서";
        List<Area> areaList = List.of(
                new Area(null, null, "서울 암사동 유적", "POLYGON((0 0,0 1,1 1,1 0,0 0))"),
                new Area(null, null, "서울대입구역", "POLYGON((1 1,1 2,2 2,2 1,1 1))"),
                new Area(null, null, "서울식물원·마곡나루역", "POLYGON((1 1,1 2,2 2,2 1,1 1))")
        );

        given(areaRepository.findByAreaNameContaining(anyString()))
                .willReturn(Optional.of(areaList));

        // When
        CommonResponse<ResGetAreaListByKeywordDTO> response = areaService.getAreaListByKeyword(query);

        // Then
        assertTrue(response.isSuccess());
        assertEquals("장소 검색 성공", response.getMessage());
        assertEquals(3, response.getData().getAreaList().size());

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(json);
    }

    @Test
    @DisplayName("[성공] 전체 장소 리스트 조회 Service")
    void getAllAreaList() throws JsonProcessingException {
        // Given
        List<Area> areaList = List.of(
                new Area(null, null, "A", "POLYGON((0 0,0 1,1 1,1 0,0 0))"),
                new Area(null, null, "B", "POLYGON((1 1,1 2,2 2,2 1,1 1))")
        );

        given(areaRepository.findAll()).willReturn(areaList);

        // When
        CommonResponse<List<AreaDTO>> response = areaService.getAllAreaList();

        // Then
        assertTrue(response.isSuccess());
        assertEquals("전체 장소 리스트 조회 성공", response.getMessage());
        assertEquals(2, response.getData().size());

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(json);
    }

    @Test
    @DisplayName("[성공] 전체 장소 혼잡도 조회 Service")
    void getAllAreasWithCongestionLevel() throws JsonProcessingException {
        // Given
        List<AreaWithCongestionLevelDTO> dtoList = List.of(
                new AreaWithCongestionLevelDTO(null, null, "A", "POLYGON((0 0,0 1,1 1,1 0,0 0))", "혼잡"),
                new AreaWithCongestionLevelDTO(null, null,"B", "POLYGON((1 1,1 2,2 2,2 1,1 1))", "보통")
        );

        given(areaRepository.findAllAreasWithCongestionLevel()).willReturn(dtoList);

        // When
        CommonResponse<List<ResGetAreaWithCongestionLevelDTO>> response = areaService.getAllAreasWithCongestionLevel();

        // Then
        assertTrue(response.isSuccess());
        assertEquals("전체 장소 혼잡도 조회 성공", response.getMessage());
        assertEquals(2, response.getData().size());

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(json);
    }

    @Test
    @DisplayName("[성공] 전체 장소 날씨 조회 Service")
    void getAllAreasWithWeather() throws JsonProcessingException {
        // Given
        List<AreaWithWeatherDTO> dtoList = List.of(
                new AreaWithWeatherDTO(null, null, "A", "POLYGON((0 0,0 1,1 1,1 0,0 0))", "맑음", "24°C"),
                new AreaWithWeatherDTO(null, null, "B", "POLYGON((1 1,1 2,2 2,2 1,1 1))", "흐림", "20°C")
        );

        given(areaRepository.findAllAreasWithWeather()).willReturn(dtoList);

        // When
        CommonResponse<List<ResGetAreaWithWeatherDTO>> response = areaService.getAllAreasWithWeather();

        // Then
        assertTrue(response.isSuccess());
        assertEquals("전체 장소 날씨 조회 성공", response.getMessage());
        assertEquals(2, response.getData().size());

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(json);
    }

    @Test
    @DisplayName("[실패] 현위치 기반 장소 리스트 조회 Service")
    void getAreaListByCurrentLocation_failure() {
        // Given
        ReqGetAreaListByCurrentLocationDTO request = new ReqGetAreaListByCurrentLocationDTO(37.0, 127.0);

        given(locationChecker.findLocations(anyDouble(), anyDouble()))
                .willThrow(new RuntimeException("Location service failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> areaService.getAreaListByCurrentLocation(request));
        assertEquals("현위치 인근 장소 조회에 실패했습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("[실패] 장소 검색 Service - 데이터 없음")
    void getAreaListByKeyword_entityNotFound() {
        // Given
        String query = "존재하지 않는 장소명";

        given(areaRepository.findByAreaNameContaining(anyString()))
                .willReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> areaService.getAreaListByKeyword(query));
        assertEquals("장소 검색에 실패했습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("[실패] 장소 검색 Service - WKT 파싱 오류")
    void getAreaListByKeyword_invalidWkt() {
        String query = "정상 키워드";
        // Given
        List<Area> areaList = List.of(
                new Area(null, null, "A", "INVALID_WKT")
        );

        given(areaRepository.findByAreaNameContaining(anyString())).willReturn(Optional.of(areaList));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> areaService.getAreaListByKeyword(query));
        assertTrue(exception.getMessage().contains("Invalid polygon data for area"));
    }

    @Test
    @DisplayName("[실패] 전체 장소 리스트 조회 Service - WKT 파싱 오류")
    void getAllAreaList_invalidWkt() {
        // Given
        List<Area> areaList = List.of(
                new Area(null, null, "A", "INVALID_WKT")
        );

        given(areaRepository.findAll()).willReturn(areaList);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> areaService.getAllAreaList());
        assertTrue(exception.getMessage().contains("Invalid polygon data for area"));
    }

    @Test
    @DisplayName("[실패] 전체 장소 혼잡도 조회 Service - WKT 파싱 오류")
    void getAllAreasWithCongestionLevel_invalidWkt() {
        // Given
        List<AreaWithCongestionLevelDTO> dtoList = List.of(
                new AreaWithCongestionLevelDTO(null, null, "A", "INVALID_WKT", "혼잡")
        );

        given(areaRepository.findAllAreasWithCongestionLevel()).willReturn(dtoList);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> areaService.getAllAreasWithCongestionLevel());
        assertTrue(exception.getMessage().contains("Invalid polygon data for area"));
    }

    @Test
    @DisplayName("[실패] 전체 장소 날씨 조회 Service - WKT 파싱 오류")
    void getAllAreasWithWeather_invalidWkt() {
        // Given
        List<AreaWithWeatherDTO> dtoList = List.of(
                new AreaWithWeatherDTO(null, null, "A", "INVALID_WKT", "24°C", "맑음")
        );

        given(areaRepository.findAllAreasWithWeather()).willReturn(dtoList);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> areaService.getAllAreasWithWeather());
        assertTrue(exception.getMessage().contains("Invalid polygon data for area"));
    }
}
