package org.example.what_seoul.integration.citydata;

import jakarta.transaction.Transactional;
import org.example.what_seoul.WhatSeoulApplication;
import org.example.what_seoul.config.WebSecurityTestWithH2Config;
import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.domain.citydata.event.CultureEvent;
import org.example.what_seoul.domain.citydata.population.Population;
import org.example.what_seoul.domain.citydata.weather.Weather;
import org.example.what_seoul.repository.area.AreaRepository;
import org.example.what_seoul.repository.citydata.event.CultureEventRepository;
import org.example.what_seoul.repository.citydata.population.PopulationRepository;
import org.example.what_seoul.repository.citydata.weather.WeatherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = WhatSeoulApplication.class)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test-h2")
@Import(WebSecurityTestWithH2Config.class)
class CitydataIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private PopulationRepository populationRepository;

    @Autowired
    private WeatherRepository weatherRepository;

    @Autowired
    private CultureEventRepository cultureEventRepository;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void setUp() {
        // 테스트용 Area, Population, Weather, CultureEvent 엔티티 저장
        Area area = new Area(
                "test",
                "test",
                "서울",
                "POLYGON((127.0 37.0, 127.1 37.0, 127.1 37.1, 127.0 37.1, 127.0 37.0))"
        );
        areaRepository.save(area);

        populationRepository.save(new Population(
                "여유",
                "사람이 몰려있을 가능성이 낮아요.",
                "8500",
                "8000",
                "2025-04-29 16:35",
                area
        ));
        weatherRepository.save(new Weather(
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
        ));
        cultureEventRepository.saveAll(List.of(
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
        ));
    }

    @Test
    @DisplayName("[성공] 장소별 인구 현황 조회 API 통합 테스트")
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    void getPopulationData() throws Exception {
        Long areaId = areaRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/citydata/population/{areaId}", areaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.congestionLevel").value("여유"));
    }

    @Test
    @DisplayName("[성공] 장소별 날씨 현황 조회 API 통합 테스트")
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    void getWeatherData() throws Exception {
        Long areaId = areaRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/citydata/weather/{areaId}", areaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.temperature").value("18°C"));
    }

    @Test
    @DisplayName("[성공] 장소별 문화행사 조회 API 통합 테스트")
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    void getCultureEventData() throws Exception {
        Long areaId = areaRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/citydata/event/{areaId}", areaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].eventName").value("행사1"))
                .andExpect(jsonPath("$.data[0].areaId").value(areaId))
                .andExpect(jsonPath("$.data[1].eventName").value("행사2"))
                .andExpect(jsonPath("$.data[1].areaId").value(areaId));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 areaId로 인구 데이터 요청 시 404 응답")
    @WithMockUser(username = "test", roles = {"ADMIN", "USER"})
    void getPopulationData_entityNotFound() throws Exception {
        Long invalidAreaId = 9999L;

        mockMvc.perform(get("/api/citydata/population/{areaId}", invalidAreaId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Entity Not Found"))
                .andExpect(jsonPath("$.context").value("인구 현황 데이터를 찾지 못했습니다."));
    }

    @Test
    @DisplayName("[실패] 인증되지 않은 사용자 접근 시 403 응답")
    void getPopulationData_unauthenticated() throws Exception {
        Long areaId = areaRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/citydata/population/{areaId}", areaId))
                .andExpect(status().isForbidden());
    }


}
