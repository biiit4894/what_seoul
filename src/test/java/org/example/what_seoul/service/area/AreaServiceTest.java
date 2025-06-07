package org.example.what_seoul.service.area;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.area.dto.*;
import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.domain.citydata.event.CultureEvent;
import org.example.what_seoul.domain.user.User;
import org.example.what_seoul.repository.area.AreaRepository;
import org.example.what_seoul.repository.citydata.event.CultureEventRepository;
import org.example.what_seoul.service.user.UserService;
import org.example.what_seoul.service.user.dto.LoginUserInfoDTO;
import org.example.what_seoul.util.LocationChecker;
import org.example.what_seoul.util.PolygonParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class AreaServiceTest {
    @InjectMocks
    private AreaService areaService;

    @Mock
    private AreaRepository areaRepository;

    @Mock
    private CultureEventRepository cultureEventRepository;

    @Mock
    private UserService userService;

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
    @DisplayName("[성공] 전체 장소 문화행사 조회 Service")
    void getAllAreasWithCultureEvent() {
        // Given
        Area area1 = new Area(null, null, "강남 장소 A", "POLYGON((0 0,0 1,1 1,1 0,0 0))");
        Area area2 = new Area(null, null, "강남 장소 B", "POLYGON((1 1,1 2,2 2,2 1,1 1))");
        ReflectionTestUtils.setField(area1, "id", 1L); // 테스트 환경에서 id 값 설정하기
        ReflectionTestUtils.setField(area2, "id", 2L); // 테스트 환경에서 id 값 설정하기

        CultureEvent cultureEvent1 = new CultureEvent(
                "행사 A",
                "2025-01-01~2025-06-01",
                "강남 문화센터",
                "37.5",
                "127.0",
                "이미지 경로",
                "https://example.com",
                area1
        );
        CultureEvent cultureEvent2 = new CultureEvent(
                "행사 B",
                "2025-03-14~2024-07-10",
                "강남 전시회장",
                "36.9",
                "127.0",
                "이미지 경로",
                "https://example.com",
                area1
        );
        CultureEvent cultureEvent3 = new CultureEvent(
                "행사 C",
                "2025-03-14~2024-07-10",
                "강남 전시회장",
                "36.9",
                "127.0",
                "이미지 경로",
                "https://example.com",
                area2
        );

        List<Area> areas = List.of(area1, area2);
        List<CultureEvent> cultureEventList = List.of(cultureEvent1, cultureEvent2, cultureEvent3);

        when(areaRepository.findAll()).thenReturn(areas);
        when(cultureEventRepository.findAllWithArea()).thenReturn(cultureEventList);

        // Polygon Stub
        Coordinate[] coordinates = {
                new Coordinate(0, 0),
                new Coordinate(0, 1),
                new Coordinate(1, 1),
                new Coordinate(1, 0),
                new Coordinate(0, 0) // 시작점과 동일하게 닫음(IllegalArgumentException: Points of LinearRing do not form a closed linestring 방지)
        };
        Polygon polygon = new GeometryFactory().createPolygon(coordinates);

        try (MockedStatic<PolygonParser> mockedParser = Mockito.mockStatic(PolygonParser.class)) {
            mockedParser.when(() -> PolygonParser.parse(anyString(), anyString())).thenReturn(polygon);

            // When
            CommonResponse<List<ResGetAreaWithCultureEventDTO>> response = areaService.getAllAreasWithCultureEvent();

            // Then
            assertTrue(response.isSuccess());
            assertEquals("전체 장소 문화행사 조회 성공", response.getMessage());
            assertEquals(2, response.getData().size());

            ResGetAreaWithCultureEventDTO dto1 = response.getData().get(0);
            assertEquals("강남 장소 A", dto1.getAreaName());
            assertEquals(2, dto1.getCultureEventList().size());

            ResGetAreaWithCultureEventDTO dto2 = response.getData().get(1);
            assertEquals("강남 장소 B", dto2.getAreaName());
            assertEquals(1, dto2.getCultureEventList().size());

            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
            System.out.println(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("[성공] 후기를 작성한 장소 이름 목록 조회 Service")
    void getAreaNamesWithMyBoards() throws JsonProcessingException {
        // Given
        Long userId = 1L;
        User user = new User("test", "encodedPassword", "test@example.com", "작성자");
        ReflectionTestUtils.setField(user, "id", userId);

        List<String> areaNames = List.of("areaName1", "areaName2", "areaName3");

        given(userService.getLoginUserInfo()).willReturn(new LoginUserInfoDTO(user));

        given(areaRepository.findAreaNamesByUserId(userId)).willReturn(areaNames);

        // When
        CommonResponse<List<String>> response = areaService.getAreaNamesWithMyBoards();

        // Then
        assertTrue(response.isSuccess());
        assertEquals("후기를 작성한 장소 이름 목록 조회 성공", response.getMessage());
        assertTrue(response.getData().containsAll(areaNames));

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

    // 이게 정말 필요할까?
    @Test
    @DisplayName("[실패] 전체 장소 문화행사 조회 Service - Area와 문화행사 데이터가 없는 경우 ")
    void getAllAreasWithCultureEvent_noAreaAndCultureEventData() throws JsonProcessingException {
        // Given
        when(areaRepository.findAll()).thenReturn(Collections.emptyList());
        when(cultureEventRepository.findAllWithArea()).thenReturn(Collections.emptyList());

        // When
        CommonResponse<List<ResGetAreaWithCultureEventDTO>> response = areaService.getAllAreasWithCultureEvent();

        // Then
        assertTrue(response.isSuccess());
        assertEquals("전체 장소 문화행사 조회 성공", response.getMessage());
        assertTrue(response.getData().isEmpty());  // 데이터가 없으므로 빈 리스트를 반환해야 함

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(json);
    }

    @Test
    @DisplayName("[실패] 전체 장소 문화행사 조회 Service - Polygon 파싱 오류 ")
    void getAllAreasWithCultureEvent_polygonParsingError() {
        // Given
        Area area1 = new Area(null, null, "강남 장소 A", "POLYGON((0 0,0 1,1 1,1 0,0 0))");
        Area area2 = new Area(null, null, "강남 장소 B", "POLYGON((1 1,1 2,2 2,2 1,1 1))");
        ReflectionTestUtils.setField(area1, "id", 1L); // 테스트 환경에서 id 값 설정하기
        ReflectionTestUtils.setField(area2, "id", 2L); // 테스트 환경에서 id 값 설정하기

        List<Area> areaList = List.of(area1, area2);
        List<CultureEvent> cultureEventList = List.of();

        when(areaRepository.findAll()).thenReturn(areaList);
        when(cultureEventRepository.findAllWithArea()).thenReturn(cultureEventList);

        // Polygon Stub 실패
        try (MockedStatic<PolygonParser> mockedParser = Mockito.mockStatic(PolygonParser.class)) {
            mockedParser.when(() -> PolygonParser.parse(anyString(), anyString())).thenThrow(new RuntimeException("Polygon 파싱 오류"));

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> areaService.getAllAreasWithCultureEvent());

            /// Then
            assertTrue(exception.getMessage().contains("Polygon 파싱 오류"));
        }
    }

    @Test
    @DisplayName("[실패] 후기를 작성한 장소 이름 목록 조회 Service - 로그인한 사용자 정보가 없을 때")
    void getAreaNamesWithMyBoards_userNotAuthenticated() {
        // Given
        given(userService.getLoginUserInfo()).willThrow(new IllegalArgumentException("로그인한 사용자 정보가 없습니다."));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> areaService.getAreaNamesWithMyBoards()
        );

        assertEquals("로그인한 사용자 정보가 없습니다.", exception.getMessage());
    }
}
