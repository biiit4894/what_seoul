package org.example.what_seoul.service.citydata;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.citydata.dto.PlaceDTO;
import org.example.what_seoul.controller.citydata.dto.ReqLocationBasedCityDataDTO;
import org.example.what_seoul.controller.citydata.dto.ResLocationBasedCityDataDTO;
import org.example.what_seoul.controller.citydata.dto.ResCultureEventDTO;
import org.example.what_seoul.controller.citydata.dto.ResPopulationDTO;
import org.example.what_seoul.controller.citydata.dto.ResWeatherDTO;
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
import org.springframework.stereotype.Service;

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

    public CommonResponse<ResLocationBasedCityDataDTO> getLocationBasedCityData(ReqLocationBasedCityDataDTO reqLocationBasedCityDataDTO) {
        List<PlaceDTO> nearestPlaces = locationChecker.findLocations(reqLocationBasedCityDataDTO.getLatitude(), reqLocationBasedCityDataDTO.getLongitude());
        return new CommonResponse<>(
                true,
                "현위치 기반 도시데이터 조회 성공",
                new ResLocationBasedCityDataDTO(nearestPlaces)
        );

    }

}
