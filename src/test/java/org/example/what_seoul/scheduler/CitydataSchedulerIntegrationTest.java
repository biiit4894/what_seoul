package org.example.what_seoul.scheduler;

import org.example.what_seoul.domain.citydata.Area;
import org.example.what_seoul.domain.citydata.event.CultureEvent;
import org.example.what_seoul.domain.citydata.population.Population;
import org.example.what_seoul.domain.citydata.population.PopulationForecast;
import org.example.what_seoul.domain.citydata.weather.Weather;
import org.example.what_seoul.repository.area.AreaRepository;
import org.example.what_seoul.repository.citydata.event.CultureEventRepository;
import org.example.what_seoul.repository.citydata.population.PopulationForecastRepository;
import org.example.what_seoul.repository.citydata.population.PopulationRepository;
import org.example.what_seoul.repository.citydata.weather.WeatherRepository;
import org.example.what_seoul.service.citydata.CitydataParser;
import org.example.what_seoul.service.citydata.CitydataService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test-h2")
@Transactional
public class CitydataSchedulerIntegrationTest {
    @Autowired
    private CitydataScheduler citydataScheduler;

    @Autowired
    private CitydataService citydataService;

    @Autowired
    private CitydataParser citydataParser;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private PopulationRepository populationRepository;

    @Autowired
    private PopulationForecastRepository populationForecastRepository;

    @Autowired
    private WeatherRepository weatherRepository;

    @Autowired
    private CultureEventRepository cultureEventRepository;

    @Test
    @DisplayName("[성공] CitydataScheduler 통합 테스트")
    void citydataScheduler_call() {
        // given
        Area area = new Area("인구밀집지역", "POI014", "강남역", "POLYGON(...)");
        areaRepository.save(area);

       // when
        citydataScheduler.call();

        // then: 인구, 날씨, 행사 데이터가 실제로 저장되었는지 확인
        List<Population> populations = populationRepository.findAll();
        List<PopulationForecast> forecasts = populationForecastRepository.findAll();
        List<Weather> weathers = weatherRepository.findAll();
        List<CultureEvent> events = cultureEventRepository.findAll();

        assertThat(populations).isNotEmpty();
        assertThat(forecasts).isNotEmpty();
        assertThat(weathers).isNotEmpty();

        if (isUpdateCultureEventHour(LocalDateTime.now().getHour())) {
            assertThat(events).isNotEmpty();
        }
    }

    private boolean isUpdateCultureEventHour(int hour) {
        return hour % 6 == 0;
    }
}
