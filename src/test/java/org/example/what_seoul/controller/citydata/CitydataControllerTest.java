package org.example.what_seoul.controller.citydata;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.config.WebSecurityTestConfig;
import org.example.what_seoul.controller.citydata.dto.ResGetCultureEventDataDTO;
import org.example.what_seoul.controller.citydata.dto.ResGetPopulationDataDTO;
import org.example.what_seoul.controller.citydata.dto.ResGetWeatherDataDTO;
import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.domain.citydata.event.CultureEvent;
import org.example.what_seoul.domain.citydata.population.Population;
import org.example.what_seoul.domain.citydata.weather.Weather;
import org.example.what_seoul.service.citydata.CitydataService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CitydataController.class)
@ActiveProfiles("test")
@Import(WebSecurityTestConfig.class) // 테스트를 위한 custom security configuration
public class CitydataControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CitydataService citydataService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[성공] 장소별 인구 현황 데이터 조회 Controller")
    void getPopulationData() throws Exception {
        // Given
        Long areaId = 1L;
        Area area = createTestArea();
        Population population = new Population(
                "여유",
                "사람이 몰려있을 가능성이 낮아요.",
                "8500",
                "8000",
                "2025-04-29 16:35",
                area
        );
        ResGetPopulationDataDTO res = ResGetPopulationDataDTO.from(population);
        CommonResponse<ResGetPopulationDataDTO> commonResponse = new CommonResponse<>(true, "장소별 인구 현황 데이터 조회 성공", res);

        // When & Then
        when(citydataService.findPopulationDataByAreaId(areaId)).thenReturn(commonResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/citydata/population/{areaId}", areaId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.congestionLevel").value("여유"))
                .andExpect(jsonPath("$.data.congestionMessage").value("사람이 몰려있을 가능성이 낮아요."));
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[성공] 장소별 날씨 현황 데이터 조회 Controller")
    void getWeatherData() throws Exception {
        // Given
        Long areaId = 1L;
        Area area = createTestArea();

        Weather weather = new Weather(
                "18°C",
                "24°C",
                "13°C",
                "좋음",
                "12",
                "좋음",
                "10",
                "비 또는 눈 소식이 없어요.",
                "2025-04-29 16:50",
                area
        );
        ResGetWeatherDataDTO res = ResGetWeatherDataDTO.from(weather);
        CommonResponse<ResGetWeatherDataDTO> commonResponse = new CommonResponse<>(true, "장소별 날씨 현황 데이터 조회 성공", res);

        // When & Then
        when(citydataService.findWeatherDataByAreaId(areaId)).thenReturn(commonResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/citydata/weather/{areaId}", areaId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.temperature").value("18°C"))
                .andExpect(jsonPath("$.data.pcpMsg").value("비 또는 눈 소식이 없어요."));
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[성공] 장소별 문화행사 데이터 조회 Controller")
    void getCultureEventData() throws Exception {
        // Given
        Long areaId = 1L;
        Area area = createTestArea();

        List<CultureEvent> cultureEventList = List.of(
                new CultureEvent(
                        "행사1",
                        "2025-04-25~2025-09-07",
                        "주소1",
                        "127.00977973484339",
                        "37.56735731522952",
                        "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=0bdf8a8555544da9a012da6ea6f49e50&thumb=Y",
                        "https://culture.seoul.go.kr/culture/culture/cultureEvent/view.do?cultcode=152681&menuNo=200009",
                        area
                ),
                new CultureEvent(
                        "행사2",
                        "2025-04-29~2025-08-01",
                        "주소2",
                        "127.00977333484339",
                        "37.56735711522952",
                        "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=0bdf8a8555544da9a012da6ea6f49e50&thumb=Y",
                        "https://culture.seoul.go.kr/culture/culture/cultureEvent/view.do?cultcode=152681&menuNo=200009",
                        area
                )


        );
        List<ResGetCultureEventDataDTO> dtoList = ResGetCultureEventDataDTO.from(cultureEventList);
        CommonResponse<List<ResGetCultureEventDataDTO>> commonResponse = new CommonResponse<>(true, "장소별 문화행사 데이터 조회 성공", dtoList);

        // When & Then
        when(citydataService.findCultureEventDataByAreaId(areaId)).thenReturn(commonResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/citydata/event/{areaId}", areaId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].eventName").value("행사1"))
                .andExpect(jsonPath("$.data[0].areaId").value(area.getId()))
                .andExpect(jsonPath("$.data[1].eventName").value("행사2"))
                .andExpect(jsonPath("$.data[1].areaId").value(area.getId()));
    }

    private static Area createTestArea() {
        return new Area(
                "POI050",
                "천호역",
                "인구밀집지역",
                "POLYGON ((127.12782542009622 37.54031451897783, 127.12784181342528 37.53998366374988, 127.12741614871513 37.53897810766761, 127.12652374918184 37.53772998648546, 127.12591148105541 37.536942465411364, 127.12493751476998 37.53731306443991, 127.12392921331896 37.537719234202996, 127.12339549385793 37.53798986463069, 127.12255616680405 37.538038001191474, 127.12199691548906 37.53825463748036, 127.12249874785249 37.53921962356995, 127.12290693539497 37.53994839236029, 127.12334881386708 37.54053533696927, 127.123391521014 37.54072434233456, 127.12368900480186 37.541156145338256, 127.12396925407013 37.54141917105127, 127.12782542009622 37.54031451897783))"
        );
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 존재하지 않는 URL 요청 시 404 Not Found 응답")
    void nonExistingCitydataUrl() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/citydata/non-existing-endpoint"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("[실패] 장소별 인구 현황 데이터 조회 Controller - 로그인 없이 호출 시 403 Access Denied 응답")
    void getPopulationData_unauthorized() throws Exception {
        Long areaId = 1L;
        mockMvc.perform(MockMvcRequestBuilders.get("/api/citydata/population/{areaId}", areaId))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 장소별 인구 현황 데이터 조회 Controller - 잘못된 HTTP 메서드로 요청 시 405 Method Not Allowed 응답")
    void getPopulationData_wrongHttpMethod() throws Exception {
        Long areaId = 1L;
        mockMvc.perform(MockMvcRequestBuilders.put("/api/citydata/population/{areaId}", areaId))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());

    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 장소별 인구 현황 데이터 조회 Controller - path variable 미전달로 인한 404 Not Found 응답")
    void getPopulationData_noPathVar() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/citydata/population"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 장소별 인구 현황 데이터 조회 Controller - 존재하지 않는 지역 ID 전달로 인한 404 Not Found 응답")
    void getPopulationData_dataNotFound() throws Exception {
        Long areaId = 99L;

        when(citydataService.findPopulationDataByAreaId(areaId))
                .thenThrow(new EntityNotFoundException("인구 현황 데이터를 찾지 못했습니다."));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/citydata/population/{areaId}", areaId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("[실패] 장소별 날씨 현황 데이터 조회 Controller - 로그인 없이 호출 시 403 Access Denied 응답")
    void getWeatherData_unauthorized() throws Exception {
        Long areaId = 1L;
        mockMvc.perform(MockMvcRequestBuilders.get("/api/citydata/weather/{areaId}", areaId))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 장소별 날씨 현황 데이터 조회 Controller - 잘못된 HTTP 메서드로 요청 시 405 Method Not Allowed 응답")
    void getWeatherData_wrongHttpMethod() throws Exception {
        Long areaId = 1L;
        mockMvc.perform(MockMvcRequestBuilders.put("/api/citydata/weather/{areaId}", areaId))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 장소별 날씨 현황 데이터 조회 Controller - 존재하지 않는 지역 ID 전달로 인한 404 Not Found 응답")
    void getWeatherData_dataNotFound() throws Exception {
        Long areaId = 99L;

        when(citydataService.findWeatherDataByAreaId(areaId))
                .thenThrow(new EntityNotFoundException("날씨 현황 데이터를 찾지 못했습니다."));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/citydata/weather/{areaId}", areaId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 장소별 날씨 현황 데이터 조회 Controller - path variable 미전달로 인한 404 Not Found 응답")
    void getWeatherData_noPathVar() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/citydata/weather"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("[실패] 장소별 문화행사 데이터 조회 Controller - 로그인 없이 호출 시 403 Access Denied 응답")
    void getCultureEvent_unauthorized() throws Exception {
        Long areaId = 1L;
        mockMvc.perform(MockMvcRequestBuilders.get("/api/citydata/event/{areaId}", areaId))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 장소별 문화행사 데이터 조회 Controller - 잘못된 HTTP 메서드로 요청 시 405 Method Not Allowed 응답")
    void getCultureEvent_wrongHttpMethod() throws Exception {
        Long areaId = 1L;
        mockMvc.perform(MockMvcRequestBuilders.put("/api/citydata/event/{areaId}", areaId))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 장소별 문화행사 데이터 조회 Controller - path variable 미전달로 인한 404 Not Found 응답")
    void getCultureEvent_noPathVar() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/citydata/event"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    @DisplayName("[실패] 장소별 문화행사 데이터 조회 Controller - 존재하지 않는 지역 ID 전달로 인한 404 Not Found 응답")
    void getCultureEvent_dataNotFound() throws Exception {
        Long areaId = 99L;

        when(citydataService.findCultureEventDataByAreaId(areaId))
                .thenThrow(new EntityNotFoundException("문화행사 데이터를 찾지 못했습니다."));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/citydata/event/{areaId}", areaId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}

