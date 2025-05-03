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
import org.example.what_seoul.service.citydata.PcpMsgHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class CitydataSchedulerTest {
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
    @Mock
    private PcpMsgHistoryService pcpMsgHistoryService;

    @InjectMocks
    private CitydataScheduler citydataScheduler;

    private CitydataScheduler schedulerSpy;

    @BeforeEach
    void setup() {
        schedulerSpy = Mockito.spy(citydataScheduler);
    }

    @Test
    @DisplayName("[성공] 도시데이터 저장 - CitydataScheduler call()")
    void call() {
        // Given
        Area area = new Area(
                "POI050",
                "천호역",
                "인구밀집지역",
                "POLYGON ((127.12782542009622 37.54031451897783, 127.12784181342528 37.53998366374988, 127.12741614871513 37.53897810766761, 127.12652374918184 37.53772998648546, 127.12591148105541 37.536942465411364, 127.12493751476998 37.53731306443991, 127.12392921331896 37.537719234202996, 127.12339549385793 37.53798986463069, 127.12255616680405 37.538038001191474, 127.12199691548906 37.53825463748036, 127.12249874785249 37.53921962356995, 127.12290693539497 37.53994839236029, 127.12334881386708 37.54053533696927, 127.123391521014 37.54072434233456, 127.12368900480186 37.541156145338256, 127.12396925407013 37.54141917105127, 127.12782542009622 37.54031451897783))"
        );

        CityData cityData = mock(CityData.class);
        when(cityData.getPopulation()).thenReturn(mock(Population.class));
        when(cityData.getPopulationForecast()).thenReturn(List.of(mock(PopulationForecast.class)));
        when(cityData.getWeather()).thenReturn(mock(Weather.class));

        when(areaRepository.findAll()).thenReturn(List.of(area));

        // schedulerSpy.fetchCityData()가 호출되면 실제 메서드를 실행하는 대신, future라는 미리 준비한 결과를 그대로 반환
        CompletableFuture<CityData> future = CompletableFuture.completedFuture(cityData);
        doReturn(future).when(schedulerSpy).fetchCityData(eq(area), anyBoolean());

        // When
        schedulerSpy.call();

        // Then
        verify(populationRepository).saveAll(anyList());
        verify(populationForecastRepository).saveAll(anyList());
        verify(weatherRepository).saveAll(anyList());
        // 문화 이벤트는 특정 시간대에만 저장되므로, 호출되었는지는 상황에 따라 다름
    }

    @Test
    @DisplayName("[실페] 도시데이터 저장 - CitydataScheduler call()")
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
