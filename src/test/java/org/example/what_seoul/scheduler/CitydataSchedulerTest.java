package org.example.what_seoul.scheduler;

import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.domain.citydata.CityData;
import org.example.what_seoul.domain.citydata.event.CultureEvent;
import org.example.what_seoul.domain.citydata.population.Population;
import org.example.what_seoul.domain.citydata.population.PopulationForecast;
import org.example.what_seoul.domain.citydata.weather.Weather;
import org.example.what_seoul.exception.CitydataSchedulerException;
import org.example.what_seoul.repository.area.AreaRepository;
import org.example.what_seoul.repository.citydata.event.CultureEventRepository;
import org.example.what_seoul.repository.citydata.population.PopulationForecastRepository;
import org.example.what_seoul.repository.citydata.population.PopulationRepository;
import org.example.what_seoul.repository.citydata.weather.WeatherRepository;
import org.example.what_seoul.service.citydata.CitydataParser;
import org.example.what_seoul.service.citydata.CitydataService;
//import org.example.what_seoul.service.citydata.PcpMsgHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class CitydataSchedulerTest {
    @Mock
    private CitydataService citydataService;

    @Mock
    private CitydataParser citydataParser;
    @Mock
    private AreaRepository areaRepository;
    @Mock
    private PopulationRepository populationRepository;
    @Mock
    private PopulationForecastRepository populationForecastRepository;
    @Mock
    private WeatherRepository weatherRepository;
    @Mock
    private CultureEventRepository cultureEventRepository;
//    @Mock
//    private PcpMsgHistoryService pcpMsgHistoryService;

    @InjectMocks
    private CitydataScheduler citydataScheduler;

    private CitydataScheduler schedulerSpy;

    @BeforeEach
    void setup() {
        schedulerSpy = Mockito.spy(new CitydataScheduler(
                citydataService,
                citydataParser,
                areaRepository,
                populationRepository,
                populationForecastRepository,
                weatherRepository,
                cultureEventRepository
        ));
    }

    @Test
    @DisplayName("[성공] CitydataScheduler - 도시데이터 저장")
    void call() {
        // Given
        Area area = new Area(
                "인구밀집지역",
                "POI050",
                "천호역",
                "POLYGON ((127.12782542009622 37.54031451897783, 127.12784181342528 37.53998366374988, 127.12741614871513 37.53897810766761, 127.12652374918184 37.53772998648546, 127.12591148105541 37.536942465411364, 127.12493751476998 37.53731306443991, 127.12392921331896 37.537719234202996, 127.12339549385793 37.53798986463069, 127.12255616680405 37.538038001191474, 127.12199691548906 37.53825463748036, 127.12249874785249 37.53921962356995, 127.12290693539497 37.53994839236029, 127.12334881386708 37.54053533696927, 127.123391521014 37.54072434233456, 127.12368900480186 37.541156145338256, 127.12396925407013 37.54141917105127, 127.12782542009622 37.54031451897783))"
        );

        CityData cityData = mock(CityData.class);
        Population population = mock(Population.class);
        PopulationForecast populationForecast = mock(PopulationForecast.class);
        Weather weather = mock(Weather.class);

        when(cityData.getPopulation()).thenReturn(population);
        when(cityData.getPopulationForecast()).thenReturn(List.of(populationForecast));
        when(cityData.getWeather()).thenReturn(weather);

        when(areaRepository.findAll()).thenReturn(List.of(area));

        CompletableFuture<CityData> future = CompletableFuture.completedFuture(cityData);
        doReturn(future).when(schedulerSpy).fetchCityData(eq(area), anyBoolean());

        doAnswer(invocation -> {
            List<Population> populations = invocation.getArgument(0);
            List<PopulationForecast> forecasts = invocation.getArgument(1);
            List<Weather> weathers = invocation.getArgument(2);

            populationRepository.saveAll(populations);
            populationForecastRepository.saveAll(forecasts);
            weatherRepository.saveAll(weathers);

            return null;
        }).when(citydataService).updatePopulationAndWeatherData(anyList(), anyList(), anyList());

        // When
        schedulerSpy.call();

        // Then
        verify(populationRepository).saveAll(anyList());
        verify(populationForecastRepository).saveAll(anyList());
        verify(weatherRepository).saveAll(anyList());

        verify(citydataService).updatePopulationAndWeatherData(anyList(), anyList(), anyList());

        if (LocalDateTime.now().getHour() % 6 == 0) {
            verify(citydataService).updateOrInsertCultureEventData(anyList());
        } else {
            verify(citydataService, never()).updateOrInsertCultureEventData(anyList());
        }
    }


    @Test
    @DisplayName("[성공] CitydataScheduler - 도시데이터 fetch")
    void fetchCityData() throws Exception {
        // Given
        Area area = new Area("인구밀집지역", "POI050", "천호역", "POLYGON(...)");
        ReflectionTestUtils.setField(area, "id", 1L);
        Document document = mock(Document.class);

        doReturn(document).when(schedulerSpy).getDocument(any());

        Population mockPopulation = mock(Population.class);
        List<PopulationForecast> mockForecasts = List.of(mock(PopulationForecast.class));
        Weather mockWeather = mock(Weather.class);
        List<CultureEvent> mockCultureEvents = List.of(mock(CultureEvent.class));

        // 해당 파서 메소드들이 내부에서 호출되므로 spy로 설정
        when(citydataParser.parsePopulationData(any(), eq(area))).thenReturn(mockPopulation);
        when(citydataParser.parsePopulationForecastData(any(), eq(mockPopulation), eq(area))).thenReturn(mockForecasts);
        when(citydataParser.parseWeatherData(any(), eq(area))).thenReturn(mockWeather);
        when(citydataParser.parseCultureEventData(any(), eq(area))).thenReturn(mockCultureEvents);

        // When
        CompletableFuture<CityData> future = schedulerSpy.fetchCityData(area, true);
        CityData result = future.get(3, TimeUnit.SECONDS);

        // Then
        assertNotNull(result);
        assertEquals(mockPopulation, result.getPopulation());
        assertEquals(mockForecasts, result.getPopulationForecast());
        assertEquals(mockWeather, result.getWeather());
        assertEquals(mockCultureEvents, result.getCultureEvent());
    }


    @Test
    @DisplayName("[실패] 도시데이터 저장 - CitydataScheduler call()")
    void call_throwCompletionException() {
        // Given
        Area area = new Area(
                "POI050",
                "천호역",
                "인구밀집지역",
                "POLYGON ((127.12782542009622 37.54031451897783, 127.12784181342528 37.53998366374988, 127.12741614871513 37.53897810766761, 127.12652374918184 37.53772998648546, 127.12591148105541 37.536942465411364, 127.12493751476998 37.53731306443991, 127.12392921331896 37.537719234202996, 127.12339549385793 37.53798986463069, 127.12255616680405 37.538038001191474, 127.12199691548906 37.53825463748036, 127.12249874785249 37.53921962356995, 127.12290693539497 37.53994839236029, 127.12334881386708 37.54053533696927, 127.123391521014 37.54072434233456, 127.12368900480186 37.541156145338256, 127.12396925407013 37.54141917105127, 127.12782542009622 37.54031451897783))"
        );

        when(areaRepository.findAll()).thenReturn(List.of(area));

        // schedulerSpy.fetchCityData() 가 호출되면 CitydataSchedulerException을 감싼 실패한 CompletableFuture를 리턴
        doReturn(CompletableFuture.failedFuture(new CitydataSchedulerException("Fetch error")))
                .when(schedulerSpy).fetchCityData(eq(area), anyBoolean());

        // When & Then
        CompletionException exception = assertThrows(CompletionException.class, () -> schedulerSpy.call());
        assertThrows(CitydataSchedulerException.class, () -> {throw exception.getCause();}); // 예외는 CompletionException 내부에 wrapping 되어서 발생하기 때문에 cause를 검색
    }
}
