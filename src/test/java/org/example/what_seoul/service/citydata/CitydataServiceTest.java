package org.example.what_seoul.service.citydata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.citydata.dto.ResGetCultureEventDataDTO;
import org.example.what_seoul.controller.citydata.dto.ResGetPopulationDataDTO;
import org.example.what_seoul.controller.citydata.dto.ResGetWeatherDataDTO;
import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.domain.citydata.event.CultureEvent;
import org.example.what_seoul.domain.citydata.population.Population;
import org.example.what_seoul.domain.citydata.population.PopulationForecast;
import org.example.what_seoul.domain.citydata.weather.Weather;
import org.example.what_seoul.exception.DatabaseException;
import org.example.what_seoul.repository.board.BoardRepository;
import org.example.what_seoul.repository.citydata.event.CultureEventRepository;
import org.example.what_seoul.repository.citydata.population.PopulationForecastRepository;
import org.example.what_seoul.repository.citydata.population.PopulationRepository;
import org.example.what_seoul.repository.citydata.weather.WeatherRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class CitydataServiceTest {
    @InjectMocks
    private CitydataService citydataService;

    @Mock
    private PopulationRepository populationRepository;

    @Mock
    private PopulationForecastRepository populationForecastRepository;

    @Mock
    private WeatherRepository weatherRepository;

    @Mock
    private CultureEventRepository cultureEventRepository;

    @Mock
    private BoardRepository boardRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CitydataServiceTest() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("[성공] 장소별 인구 현황 데이터 조회 Service")
    void findPopulationDataByAreaId() throws JsonProcessingException {
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

        // When
        when(populationRepository.findByAreaId(areaId)).thenReturn(Optional.of(population));
        CommonResponse<ResGetPopulationDataDTO> response = citydataService.findPopulationDataByAreaId(areaId);

        // Then
        assertTrue(response.isSuccess());
        assertEquals("장소별 인구 현황 데이터 조회 성공", response.getMessage());
        assertEquals("여유", response.getData().getCongestionLevel());
        assertEquals(area.getId(), response.getData().getAreaId());

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(json);
    }

    @Test
    @DisplayName("[성공] 장소별 날씨 현황 데이터 조회 Service")
    void findWeatherDataByAreaId() throws JsonProcessingException {
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

        // When
        when(weatherRepository.findByAreaId(areaId)).thenReturn(Optional.of(weather));
        CommonResponse<ResGetWeatherDataDTO> response = citydataService.findWeatherDataByAreaId(areaId);

        // Then
        assertTrue(response.isSuccess());
        assertEquals("장소별 날씨 현황 데이터 조회 성공", response.getMessage());
        assertEquals("18°C", response.getData().getTemperature());
        assertEquals("비 또는 눈 소식이 없어요.", response.getData().getPcpMsg());
        assertEquals(area.getId(), response.getData().getAreaId());

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(json);
    }

    @Test
    @DisplayName("[성공] 장소별 문화행사 데이터 조회 Service")
    void findCultureEventDataByAreaId() throws JsonProcessingException {
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

        // When
        when(cultureEventRepository.findAllByAreaIdIsOrderByIsEndedAsc(areaId)).thenReturn(Optional.of(cultureEventList));
        CommonResponse<List<ResGetCultureEventDataDTO>> response = citydataService.findCultureEventDataByAreaId(areaId);

        // Then
        assertTrue(response.isSuccess());
        assertEquals("장소별 문화행사 데이터 조회 성공", response.getMessage());
        assertEquals(2, response.getData().size());

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        System.out.println(json);
    }

    private static Area createTestArea() {
        return new Area(
                "인구밀집지역",
                "POI050",
                "천호역",
                "POLYGON ((127.12782542009622 37.54031451897783, 127.12784181342528 37.53998366374988, 127.12741614871513 37.53897810766761, 127.12652374918184 37.53772998648546, 127.12591148105541 37.536942465411364, 127.12493751476998 37.53731306443991, 127.12392921331896 37.537719234202996, 127.12339549385793 37.53798986463069, 127.12255616680405 37.538038001191474, 127.12199691548906 37.53825463748036, 127.12249874785249 37.53921962356995, 127.12290693539497 37.53994839236029, 127.12334881386708 37.54053533696927, 127.123391521014 37.54072434233456, 127.12368900480186 37.541156145338256, 127.12396925407013 37.54141917105127, 127.12782542009622 37.54031451897783))"
        );
    }

    @Test
    @DisplayName("[성공] 장소별 인구 및 날씨 현황 데이터 갱신 Service")
    void updatePopulationAndWeatherData() {
        // given
        List<Population> populationList = List.of(mock(Population.class), mock(Population.class));
        List<PopulationForecast> forecastList = List.of(mock(PopulationForecast.class));
        List<Weather> weatherList = List.of(mock(Weather.class), mock(Weather.class), mock(Weather.class));

        // when
        citydataService.updatePopulationAndWeatherData(populationList, forecastList, weatherList);

        // then
        verify(populationForecastRepository, times(1)).deleteAllInBatch();
        verify(populationRepository, times(1)).deleteAllInBatch();
        verify(weatherRepository, times(1)).deleteAllInBatch();

        verify(populationRepository, times(1)).saveAll(populationList);
        verify(populationForecastRepository, times(1)).saveAll(forecastList);
        verify(weatherRepository, times(1)).saveAll(weatherList);
    }

    @Test
    @DisplayName("[성공] 장소별 문화행사 데이터 갱신 Service")
    void updateOrInsertCultureEventData() {
        // given
        Area areaA = new Area(
                "인구밀집지역",
                "POI001",
                "테스트지역A",
                "POLYGON ((127.1 37.5, 127.1 37.6, 127.2 37.6, 127.2 37.5, 127.1 37.5))"
        );

        Area areaB = new Area(
                "인구밀집지역",
                "POI002",
                "테스트지역B",
                "POLYGON ((127.3 37.7, 127.3 37.8, 127.4 37.8, 127.4 37.7, 127.3 37.7))"
        );

        // 기존 저장된 이벤트
        CultureEvent existingEvent1 = spy(new CultureEvent(
                "ExistingEvent",
                "2023-01-01~2023-12-31",
                "주소1",
                "127.123",
                "37.123",
                "thumb.png",
                "url1.com",
                areaB
        ));

        CultureEvent existingEvent2 = spy(new CultureEvent(
                "OtherEvent",
                "2023-03-01~2023-10-31",
                "주소2",
                "127.456",
                "37.456",
                "thumb2.png",
                "url2.com",
                areaA
        ));

        List<CultureEvent> existingEvents = List.of(existingEvent1, existingEvent2);

        // 새로 수집된 이벤트
        CultureEvent fetchedEvent1 = new CultureEvent(
                "NewEvent",
                "2025-04-25~2025-09-07",
                "주소1",
                "127.00977973484339",
                "37.56735731522952",
                "https://example.com/image1.png",
                "https://example.com/event1",
                areaA
        );

        CultureEvent fetchedEvent2 = new CultureEvent(
                "ExistingEvent",
                "2025-05-01~2025-10-01",
                "주소업데이트",
                "127.789",
                "37.789",
                "https://example.com/image2.png",
                "https://example.com/event2",
                areaB
        );

        // mocking
        when(cultureEventRepository.findAll()).thenReturn(existingEvents);

        // isEnded 갱신 관련
        doReturn(true).when(existingEvent1).evaluateIsEnded();
        doReturn(true).when(existingEvent1).updateIsEnded(true);

        doReturn(false).when(existingEvent2).evaluateIsEnded();
        doReturn(false).when(existingEvent2).updateIsEnded(false);

        // DB에 존재 여부 확인
        when(cultureEventRepository.findByEventNameAndArea("NewEvent", areaA)).thenReturn(Optional.empty());
        when(cultureEventRepository.findByEventNameAndArea("ExistingEvent", areaB)).thenReturn(Optional.of(existingEvent1));

        // updateFrom 처리
        doReturn(true).when(existingEvent1).updateFrom(fetchedEvent2);

        // when
        citydataService.updateOrInsertCultureEventData(List.of(fetchedEvent1, fetchedEvent2));

        // then
        verify(existingEvent1).evaluateIsEnded();
        verify(existingEvent1).updateIsEnded(true);
        verify(existingEvent2).evaluateIsEnded();
        verify(existingEvent2).updateIsEnded(false);
        verify(cultureEventRepository, times(2)).saveAll(any());
        verify(cultureEventRepository).save(fetchedEvent1);
        verify(existingEvent1).updateFrom(fetchedEvent2);
    }

    @Test
    @DisplayName("[성공] 후기가 없는 문화행사 데이터 삭제 Service")
    void deleteExpiredCultureEventsWithoutReviews() {
        // given
        Area area = new Area(
                "테스트 타입",
                "POI001",
                "테스트 지역",
                "POLYGON (...)");

        LocalDate now = LocalDate.now();

        String expiredEventPeriod = now.minusMonths(4).withDayOfMonth(1) + "~" + now.minusMonths(4).withDayOfMonth(28);
        String recentEventPeriod = now.minusMonths(2).withDayOfMonth(1) + "~" + now.minusMonths(2).withDayOfMonth(28);
        String eventWithReviewPeriod = now.minusMonths(4).withDayOfMonth(1) + "~" + now.minusMonths(4).withDayOfMonth(15);

        // 종료일 기준 4개월 지난 행사 (삭제 대상)
        CultureEvent eventToDelete = new CultureEvent(
                "행사1",
                expiredEventPeriod,
                "주소1",
                "127.0", "37.0",
                "thumb", "url", area
        );
        ReflectionTestUtils.setField(eventToDelete, "id", 1L);

        // 종료일 기준 2개월 지난 행사 (삭제 대상이 아님)
        CultureEvent recentEvent = new CultureEvent(
                "행사2",
                recentEventPeriod,
                "주소2",
                "127.0", "37.0",
                "thumb", "url", area
        );
        ReflectionTestUtils.setField(recentEvent, "id", 2L);

        // 종료일 4개월 전인데 후기 존재 (삭제 대상이 아님)
        CultureEvent eventWithReview = new CultureEvent(
                "행사3",
                eventWithReviewPeriod,
                "주소3",
                "127.0", "37.0",
                "thumb", "url", area
        );
        ReflectionTestUtils.setField(eventWithReview, "id", 3L);

        List<CultureEvent> endedEvents = List.of(eventToDelete, recentEvent, eventWithReview);

        when(cultureEventRepository.findAllByIsEndedTrue()).thenReturn(endedEvents);
        when(boardRepository.existsByCultureEventId(1L)).thenReturn(false); // 삭제 대상
        when(boardRepository.existsByCultureEventId(3L)).thenReturn(true);  // 후기 존재

        // when
        citydataService.deleteExpiredCultureEventsWithoutReviews();

        // then
        verify(cultureEventRepository).deleteAll(argThat((ArgumentMatcher<List<CultureEvent>>) events ->
                events.size() == 1 && events.get(0).getId().equals(1L)
        ));
    }
    @Test
    @DisplayName("[실패] 장소별 인구 현황 데이터 조회 실패 - 데이터 없음")
    void findPopulationDataByAreaId_NotFound() {
        // Given
        Long areaId = 99L;

        // When
        when(populationRepository.findByAreaId(areaId)).thenReturn(Optional.empty());

        // Then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> citydataService.findPopulationDataByAreaId(areaId));

        assertEquals("인구 현황 데이터를 찾지 못했습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("[실패] 장소별 인구 현황 데이터 조회 Service - DB 에러")
    void findPopulationDataByAreaId_DatabaseException() {
        // Given
        Long areaId = 1L;

        // When
        when(populationRepository.findByAreaId(areaId)).thenThrow(new DataAccessResourceFailureException("DB Error"));

        // Then
        DatabaseException ex = assertThrows(DatabaseException.class,
                () -> citydataService.findPopulationDataByAreaId(areaId));

        assertEquals("장소별 인구 현황 데이터 조회 실패", ex.getMessage());
    }

    @Test
    @DisplayName("[실패] 장소별 날씨 현황 데이터 조회 Service - 데이터 없음")
    void findWeatherDataByAreaId_NotFound() {
        Long areaId = 99L;

        when(weatherRepository.findByAreaId(areaId)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> citydataService.findWeatherDataByAreaId(areaId));

        assertEquals("날씨 현황 데이터를 찾지 못했습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("[실패] 장소별 날씨 현황 데이터 조회 Service - DB 에러")
    void findWeatherDataByAreaId_DatabaseException() {
        Long areaId = 1L;

        when(weatherRepository.findByAreaId(areaId)).thenThrow(new DataAccessResourceFailureException("DB Error"));

        DatabaseException ex = assertThrows(DatabaseException.class,
                () -> citydataService.findWeatherDataByAreaId(areaId));

        assertEquals("장소별 날씨 현황 데이터 조회 실패", ex.getMessage());
    }

    @Test
    @DisplayName("[실패] 장소별 문화행사 데이터 조회 Service - 데이터 없음")
    void findCultureEventDataByAreaId_NotFound() {
        Long areaId = 99L;

        when(cultureEventRepository.findAllByAreaIdIsOrderByIsEndedAsc(areaId)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> citydataService.findCultureEventDataByAreaId(areaId));

        assertEquals("문화 행사 데이터를 찾지 못했습니다.", ex.getMessage());
    }

    @Test
    @DisplayName("[실패] 장소별 문화행사 데이터 조회 Service - DB 에러")
    void findCultureEventDataByAreaId_DatabaseException() {
        Long areaId = 1L;

        when(cultureEventRepository.findAllByAreaIdIsOrderByIsEndedAsc(areaId)).thenThrow(new DataAccessResourceFailureException("DB Error"));

        DatabaseException ex = assertThrows(DatabaseException.class,
                () -> citydataService.findCultureEventDataByAreaId(areaId));

        assertEquals("장소별 문화행사 데이터 조회 실패", ex.getMessage());
    }

}
