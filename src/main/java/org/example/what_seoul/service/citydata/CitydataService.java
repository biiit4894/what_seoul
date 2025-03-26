package org.example.what_seoul.service.citydata;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.citydata.population.dto.ResPopulationDTO;
import org.example.what_seoul.controller.citydata.weather.dto.ResWeatherDTO;
import org.example.what_seoul.domain.citydata.population.Population;
import org.example.what_seoul.domain.citydata.weather.Weather;
import org.example.what_seoul.repository.citydata.AreaRepository;
import org.example.what_seoul.repository.citydata.event.CultureEventRepository;
import org.example.what_seoul.repository.citydata.population.PopulationForecastRepository;
import org.example.what_seoul.repository.citydata.population.PopulationRepository;
import org.example.what_seoul.repository.citydata.weather.WeatherRepository;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class CitydataService {
    private final AreaRepository areaRepository;
    private final PopulationRepository populationRepository;
    private final PopulationForecastRepository populationForecastRepository;
    private final WeatherRepository weatherRepository;
    private final CultureEventRepository cultureEventRepository;

    public CommonResponse<ResPopulationDTO> findPopulationDataByAreaId(Long areaId) {
        Population population = populationRepository.findByAreaId(areaId).orElseThrow(() -> new EntityNotFoundException("Population data not found"));

        return new CommonResponse<>(
                true,
                "인구 현황 데이터 조회 성공",
                ResPopulationDTO.from(population)
        );
    }

    public CommonResponse<ResWeatherDTO> findWeatherDataByAreaId(Long areaId) {
        Weather weather = weatherRepository.findByAreaId(areaId).orElseThrow(() -> new EntityNotFoundException("Weather data not found"));
        return new CommonResponse<>(
                true,
                "날씨 현황 데이터 조회 성공",
                ResWeatherDTO.from(weather)
        );
    }
}
