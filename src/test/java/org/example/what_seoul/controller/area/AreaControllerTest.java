package org.example.what_seoul.controller.area;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.config.WebSecurityTestConfig;
import org.example.what_seoul.controller.area.dto.*;
import org.example.what_seoul.service.area.AreaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AreaController.class)
@ActiveProfiles("test")
@Import(WebSecurityTestConfig.class)  // 테스트를 위한 custom security configuration
public class AreaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AreaService areaService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[성공] 현위치 기반 장소 리스트 조회 Controller")
    void getAreaListByCurrentLocation() throws Exception {
        // Given
        double latitude = 37.5665;
        double longitude = 126.9780;

        ReqGetAreaListByCurrentLocationDTO requestDto = new ReqGetAreaListByCurrentLocationDTO(latitude, longitude);

        List<CoordinateDTO> coords = List.of(new CoordinateDTO(37.56, 126.97), new CoordinateDTO(37.57, 126.98));

        AreaDTO areaDto = new AreaDTO(1L, "서울 종로구 장소", coords);
        ResGetAreaListByCurrentLocationDTO responseDto = new ResGetAreaListByCurrentLocationDTO(List.of(areaDto));
        CommonResponse<ResGetAreaListByCurrentLocationDTO> commonResponse = new CommonResponse<>(
                true,
                "현위치 기반 장소 리스트 조회 성공",
                responseDto
        );

        given(areaService.getAreaListByCurrentLocation(any(ReqGetAreaListByCurrentLocationDTO.class))).willReturn(commonResponse);

        // When & Then
        mockMvc.perform(post("/api/area/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("현위치 기반 장소 리스트 조회 성공"))
                .andExpect(jsonPath("$.data.nearestPlaces[0].id").value(1L))
                .andExpect(jsonPath("$.data.nearestPlaces[0].areaName").value("서울 종로구 장소"))
                .andExpect(jsonPath("$.data.nearestPlaces[0].polygonCoords[0].lat").value(37.56))
                .andExpect(jsonPath("$.data.nearestPlaces[0].polygonCoords[0].lon").value(126.97));
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[성공] 장소 검색 Controller")
    void getAreaListByKeyword() throws Exception {
        // Given
        String query = "서울";

        List<CoordinateDTO> coords1 = List.of(new CoordinateDTO(37.5, 127.0), new CoordinateDTO(37.6, 127.1));
        List<CoordinateDTO> coords2 = List.of(new CoordinateDTO(37.57, 126.98), new CoordinateDTO(37.58, 126.99));

        AreaDTO areaDto1 = new AreaDTO(1L, "서울 강남구 장소", coords1);
        AreaDTO areaDto2 = new AreaDTO(2L, "서울 종로구 장소", coords2);

        ResGetAreaListByKeywordDTO responseDto = new ResGetAreaListByKeywordDTO(List.of(areaDto1, areaDto2));
        CommonResponse<ResGetAreaListByKeywordDTO> commonResponse = new CommonResponse<>(
                true,
                "장소 검색 성공",
                responseDto
        );

        given(areaService.getAreaListByKeyword(query)).willReturn(commonResponse);

        // When & Then
        mockMvc.perform(get("/api/area")
                        .param("query", query))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("장소 검색 성공"))
                .andExpect(jsonPath("$.data.areaList[0].id").value(1L))
                .andExpect(jsonPath("$.data.areaList[0].areaName").value("서울 강남구 장소"))
                .andExpect(jsonPath("$.data.areaList[0].polygonCoords[0].lat").value(37.5))
                .andExpect(jsonPath("$.data.areaList[0].polygonCoords[0].lon").value(127.0))
                .andExpect(jsonPath("$.data.areaList[1].id").value(2L))
                .andExpect(jsonPath("$.data.areaList[1].areaName").value("서울 종로구 장소"))
                .andExpect(jsonPath("$.data.areaList[1].polygonCoords[0].lat").value(37.57))
                .andExpect(jsonPath("$.data.areaList[1].polygonCoords[0].lon").value(126.98));
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[성공] 전체 장소 리스트 조회 Controller")
    void getAllAreaList() throws Exception {
        // Given
        List<CoordinateDTO> polygonCoords = List.of(
                new CoordinateDTO(37.56, 126.97),
                new CoordinateDTO(37.57, 126.98)
        );

        AreaDTO areaDTO1 = new AreaDTO(1L, "서울 강남구 장소", polygonCoords);
        AreaDTO areaDTO2 = new AreaDTO(2L, "서울 종로구 장소", polygonCoords);
        List<AreaDTO> areaList = List.of(areaDTO1, areaDTO2);

        CommonResponse<List<AreaDTO>> commonResponse = new CommonResponse<>(
                true,
                "전체 장소 리스트 조회 성공",
                areaList
        );

        given(areaService.getAllAreaList()).willReturn(commonResponse);

        // When & Then
        mockMvc.perform(get("/api/area/all"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("전체 장소 리스트 조회 성공"))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].areaName").value("서울 강남구 장소"))
                .andExpect(jsonPath("$.data[0].polygonCoords[0].lat").value(37.56))
                .andExpect(jsonPath("$.data[0].polygonCoords[0].lon").value(126.97))
                .andExpect(jsonPath("$.data[1].id").value(2L))
                .andExpect(jsonPath("$.data[1].areaName").value("서울 종로구 장소"));
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[성공] 전체 장소 혼잡도 조회 Controller")
    void getAllAreaListWithCongestionLevel() throws Exception {
        // Given
        List<CoordinateDTO> polygonCoords = List.of(new CoordinateDTO(37.55, 126.97), new CoordinateDTO(37.56, 126.98));

        ResGetAreaWithCongestionLevelDTO area1 = new ResGetAreaWithCongestionLevelDTO(
                101L,
                1L,
                "서울 마포구 장소",
                polygonCoords,
                "혼잡"
        );

        ResGetAreaWithCongestionLevelDTO area2 = new ResGetAreaWithCongestionLevelDTO(
                102L,
                2L,
                "서울 송파구 장소",
                polygonCoords,
                "보통"
        );

        List<ResGetAreaWithCongestionLevelDTO> areaList = List.of(area1, area2);

        CommonResponse<List<ResGetAreaWithCongestionLevelDTO>> response = new CommonResponse<>(
                true,
                "전체 장소 혼잡도 조회 성공",
                areaList
        );

        given(areaService.getAllAreasWithCongestionLevel()).willReturn(response);

        // When & Then
        mockMvc.perform(get("/api/area/all/ppltn"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("전체 장소 혼잡도 조회 성공"))
                .andExpect(jsonPath("$.data[0].populationId").value(101L))
                .andExpect(jsonPath("$.data[0].areaId").value(1L))
                .andExpect(jsonPath("$.data[0].areaName").value("서울 마포구 장소"))
                .andExpect(jsonPath("$.data[0].congestionLevel").value("혼잡"))
                .andExpect(jsonPath("$.data[0].polygonCoords[0].lat").value(37.55))
                .andExpect(jsonPath("$.data[0].polygonCoords[0].lon").value(126.97))
                .andExpect(jsonPath("$.data[1].areaName").value("서울 송파구 장소"))
                .andExpect(jsonPath("$.data[1].congestionLevel").value("보통"));
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[성공] 전체 장소 날씨 조회 Controller")
    void getAllAreaListWithWeather() throws Exception {
        // Given
        List<CoordinateDTO> polygonCoords = List.of(
                new CoordinateDTO(37.57, 126.98),
                new CoordinateDTO(37.58, 126.99)
        );

        ResGetAreaWithWeatherDTO area1 = new ResGetAreaWithWeatherDTO(
                201L,
                1L,
                "서울 강남구 장소",
                polygonCoords,
                "24°C",
                "강수 없음"
        );

        ResGetAreaWithWeatherDTO area2 = new ResGetAreaWithWeatherDTO(
                202L,
                2L,
                "서울 종로구 장소",
                polygonCoords,
                "22°C",
                "약한 비"
        );

        List<ResGetAreaWithWeatherDTO> weatherList = List.of(area1, area2);

        CommonResponse<List<ResGetAreaWithWeatherDTO>> response = new CommonResponse<>(
                true,
                "전체 장소 날씨 조회 성공",
                weatherList
        );

        given(areaService.getAllAreasWithWeather()).willReturn(response);

        // When & Then
        mockMvc.perform(get("/api/area/all/weather"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("전체 장소 날씨 조회 성공"))
                .andExpect(jsonPath("$.data[0].weatherId").value(201L))
                .andExpect(jsonPath("$.data[0].areaName").value("서울 강남구 장소"))
                .andExpect(jsonPath("$.data[0].temperature").value("24°C"))
                .andExpect(jsonPath("$.data[0].pcpMsg").value("강수 없음"))
                .andExpect(jsonPath("$.data[1].areaName").value("서울 종로구 장소"))
                .andExpect(jsonPath("$.data[1].temperature").value("22°C"))
                .andExpect(jsonPath("$.data[1].pcpMsg").value("약한 비"));
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[성공] 전체 장소 문화행사 조회 Controller")
    void getAllAreasWithCultureEvent() throws Exception {
        // Given
        List<ResGetAreaWithCultureEventDTO> responseList = List.of(
                new ResGetAreaWithCultureEventDTO(
                        1L,
                        "서울시 강남구 장소명 1",
                        List.of(
                                new CoordinateDTO(
                                        37.5,
                                        127.0
                                ),
                                new CoordinateDTO(
                                        37.5,
                                        127.1
                                )
                        ),
                        List.of(
                                new CultureEventDTO(
                                        1L,
                                        "행사 A",
                                        "2025-01-01~2025-06-01",
                                        "강남 문화센터",
                                        "37.5",
                                        "127.0",
                                        "이미지 경로",
                                        "https://example.com",
                                        false
                                ),
                                new CultureEventDTO(
                                        2L,
                                        "행사 B",
                                        "2025-03-14~2024-07-10",
                                        "강남 전시회장",
                                        "36.9",
                                        "127.0",
                                        "이미지 경로",
                                        "https://example.com",
                                        false
                                ))
                ),
                new ResGetAreaWithCultureEventDTO(
                        2L,
                        "서울시 강남구 장소명 2",
                        List.of(
                                new CoordinateDTO(
                                        37.6,
                                        127.0
                                ),
                                new CoordinateDTO(
                                        37.6,
                                        127.1
                                )
                        ),
                        List.of(
                                new CultureEventDTO(
                                        3L,
                                        "행사 A-1",
                                        "2025-01-01~2025-06-01",
                                        "강남 문화센터-1",
                                        "37.5",
                                        "127.0",
                                        "이미지 경로",
                                        "https://example.com",
                                        false
                                ),
                                new CultureEventDTO(
                                        4L,
                                        "행사 B-1",
                                        "2025-03-14~2024-07-10",
                                        "강남 전시회장-1",
                                        "36.9",
                                        "127.0",
                                        "이미지 경로",
                                        "https://example.com",
                                        false
                                ))
                )
        );

        CommonResponse<List<ResGetAreaWithCultureEventDTO>> response = new CommonResponse<>(true, "전체 장소 문화행사 조회 성공", responseList);

        given(areaService.getAllAreasWithCultureEvent()).willReturn(response);

        // When & Then
        mockMvc.perform(get("/api/area/all/event"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("전체 장소 문화행사 조회 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].areaId").value(1L))
                .andExpect(jsonPath("$.data[0].areaName").value("서울시 강남구 장소명 1"))
                .andExpect(jsonPath("$.data[0].cultureEventList").isArray())
                .andExpect(jsonPath("$.data[0].cultureEventList[0].eventName").value("행사 A"))
                .andExpect(jsonPath("$.data[0].cultureEventList[1].eventName").value("행사 B"))
                .andExpect(jsonPath("$.data[1].areaId").value(2L))
                .andExpect(jsonPath("$.data[1].areaName").value("서울시 강남구 장소명 2"))
                .andExpect(jsonPath("$.data[1].cultureEventList").isArray())
                .andExpect(jsonPath("$.data[1].cultureEventList[0].eventName").value("행사 A-1"))
                .andExpect(jsonPath("$.data[1].cultureEventList[1].eventName").value("행사 B-1"));
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 존재하지 않는 URL 요청 시 404 Not Found 응답")
    void nonExistingAreaUrl() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/area/non-existing-endpoint"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("[실패] 현위치 기반 장소 리스트 조회 Controller - 로그인 없이 호출 시 403 Access Denied 응답")
    void getAreaListByCurrentLocationUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/area/location"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 현위치 기반 장소 리스트 조회 Controller - request body 미전달로 400 Bad Request 응답")
    void getAreaListByCurrentLocation_BadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/area/location")
                        .contentType(MediaType.APPLICATION_JSON))  // 잘못된 POST 요청
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 현위치 기반 장소 리스트 조회 Controller - 잘못된 HTTP 메서드로 요청 시 405 Method Not Allowed 응답")
    void wrongHttpMethodForAreaListByCurrentLocation() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/area/location"))  // 잘못된 POST 요청
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }


    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 장소 검색 Controller - query 파라미터 미전달로 400 Bad Request 응답")
    void getAreaListByKeywordBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/area"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("[실패] 장소 검색 Controller - 로그인 없이 호출 시 403 Access Denied 응답")
    void getAreaListByKeywordUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/area"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 장소 검색 Controller - 잘못된 HTTP 메서드로 요청 시 405 Method Not Allowed 응답")
    void wrongHttpMethodForAreaListByKeyword() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/area"))  // 잘못된 POST 요청
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("[실패] 전체 장소 리스트 조회 Controller - 로그인 없이 호출 시 403 Access Denied 응답")
    void getAreaListUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/area/all"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 전체 장소 리스트 조회 Controller - 잘못된 HTTP 메서드로 요청 시 405 Method Not Allowed 응답")
    void wrongHttpMethodForAreaList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/area/all"))  // 잘못된 POST 요청
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("[실패] 전체 장소 혼잡도 조회 Controller - 로그인 없이 호출 시 403 Access Denied 응답")
    void getAllAreaListWithCongestionLevelUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/area/all/ppltn"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 전체 장소 혼잡도 조회 Controller - 잘못된 HTTP 메서드로 요청 시 405 Method Not Allowed 응답")
    void wrongHttpMethodForAllAreaListWithCongestionLevel() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/area/all/ppltn"))  // 잘못된 POST 요청
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("[실패] 전체 장소 날씨 조회 Controller - 로그인 없이 호출 시 403 Access Denied 응답")
    void getAllAreaListWithWeatherUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/area/all/weather"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 전체 장소 날씨 조회 Controller - 잘못된 HTTP 메서드로 요청 시 405 Method Not Allowed 응답")
    void wrongHttpMethodForAllAreaListWithWeather() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/area/all/weather"))  // 잘못된 POST 요청
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 전체 장소 문화행사 조회 Controller - Polygon ")
    void getAllAreasWithCultureEvent_polygonParsingError() throws Exception {
        // Given
        given(areaService.getAllAreasWithCultureEvent())
                .willThrow(new RuntimeException("Invalid polygon data for area: 서울시 강남구 장소명1"));

        // When & Then
        mockMvc.perform(get("/api/area/all/event"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.context").value("Invalid polygon data for area: 서울시 강남구 장소명1"));
    }
}
