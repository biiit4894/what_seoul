package org.example.what_seoul.service.citydata;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.citydata.ReqLocationDTO;
import org.example.what_seoul.controller.citydata.ResLocationDTO;
import org.example.what_seoul.controller.citydata.event.dto.ResCultureEventDTO;
import org.example.what_seoul.controller.citydata.population.dto.ResPopulationDTO;
import org.example.what_seoul.controller.citydata.weather.dto.ResWeatherDTO;
import org.example.what_seoul.domain.citydata.event.CultureEvent;
import org.example.what_seoul.domain.citydata.population.Population;
import org.example.what_seoul.domain.citydata.weather.Weather;
import org.example.what_seoul.repository.citydata.AreaRepository;
import org.example.what_seoul.repository.citydata.event.CultureEventRepository;
import org.example.what_seoul.repository.citydata.population.PopulationForecastRepository;
import org.example.what_seoul.repository.citydata.population.PopulationRepository;
import org.example.what_seoul.repository.citydata.weather.WeatherRepository;
import org.example.what_seoul.util.GeoJsonLoader;
import org.example.what_seoul.util.LocationChecker;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class CitydataService {
    private final AreaRepository areaRepository;
    private final PopulationRepository populationRepository;
    private final PopulationForecastRepository populationForecastRepository;
    private final WeatherRepository weatherRepository;
    private final CultureEventRepository cultureEventRepository;
    private final GeoJsonLoader geoJsonLoader;
    private final LocationChecker locationChecker;

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

    public CommonResponse<List<ResCultureEventDTO>> findCultureEventDataByAreaId(Long areaId) {
        List<CultureEvent> cultureEventList = cultureEventRepository.findAllByAreaId(areaId).orElseThrow(() -> new EntityNotFoundException("Culture Event data not found"));
        return new CommonResponse<>(
                true,
                "문화행사 데이터 조회 성공",
                ResCultureEventDTO.from(cultureEventList)
        );
    }

    public CommonResponse<ResLocationDTO> getLocationBasedCityData(ReqLocationDTO reqLocationDTO) throws Exception {
        String geoJson = geoJsonLoader.loadGeoJson();
        Boolean isInside = locationChecker.isInsideZone(reqLocationDTO.getLongitude(), reqLocationDTO.getLatitude(), geoJson);
        return new CommonResponse<>(
                true,
                "116개 장소에 포함되는지 조회 성공",
                new ResLocationDTO(isInside)
        );

    }

}
