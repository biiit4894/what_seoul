package org.example.what_seoul.service.citydata;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.citydata.dto.*;
import org.example.what_seoul.domain.citydata.event.CultureEvent;
import org.example.what_seoul.domain.citydata.population.Population;
import org.example.what_seoul.domain.citydata.population.PopulationForecast;
import org.example.what_seoul.domain.citydata.weather.Weather;
import org.example.what_seoul.exception.DatabaseException;
import org.example.what_seoul.repository.area.AreaRepository;
import org.example.what_seoul.repository.citydata.event.CultureEventRepository;
import org.example.what_seoul.repository.citydata.population.PopulationForecastRepository;
import org.example.what_seoul.repository.citydata.population.PopulationRepository;
import org.example.what_seoul.repository.citydata.weather.WeatherRepository;
import org.example.what_seoul.util.LocationChecker;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class CitydataService {
    private final PopulationRepository populationRepository;
    private final PopulationForecastRepository populationForecastRepository;
    private final WeatherRepository weatherRepository;
    private final CultureEventRepository cultureEventRepository;
    private final AreaRepository areaRepository;
    private final LocationChecker locationChecker;

    @Transactional(readOnly = true)
    public CommonResponse<ResGetPopulationDataDTO> findPopulationDataByAreaId(Long areaId) {
        try {
            Population population = populationRepository.findByAreaId(areaId).orElseThrow(() -> new EntityNotFoundException("인구 현황 데이터를 찾지 못했습니다."));

            return new CommonResponse<>(
                    true,
                    "장소별 인구 현황 데이터 조회 성공",
                    ResGetPopulationDataDTO.from(population)
            );
        } catch (DataAccessException e) {
            log.error("DB 접근 에러", e);
            throw new DatabaseException("장소별 인구 현황 데이터 조회 실패");
        }

    }

    @Transactional(readOnly = true)
    public CommonResponse<ResGetWeatherDataDTO> findWeatherDataByAreaId(Long areaId) {
        try {
            Weather weather = weatherRepository.findByAreaId(areaId).orElseThrow(() -> new EntityNotFoundException("날씨 현황 데이터를 찾지 못했습니다."));
            return new CommonResponse<>(
                    true,
                    "장소별 날씨 현황 데이터 조회 성공",
                    ResGetWeatherDataDTO.from(weather)
            );
        } catch (DataAccessException e) {
            log.error("DB 접근 에러", e);
            throw new DatabaseException("장소별 날씨 현황 데이터 조회 실패");
        }
    }

    @Transactional(readOnly = true)
    public CommonResponse<List<ResGetCultureEventDataDTO>> findCultureEventDataByAreaId(Long areaId) {
        try {
            List<CultureEvent> cultureEventList = cultureEventRepository.findAllByAreaId(areaId).orElseThrow(() -> new EntityNotFoundException("문화 행사 데이터를 찾지 못했습니다."));
            return new CommonResponse<>(
                    true,
                    "장소별 문화행사 데이터 조회 성공",
                    ResGetCultureEventDataDTO.from(cultureEventList)
            );
        } catch (DataAccessException e) {
            log.error("DB 접근 에러", e);
            throw new DatabaseException("장소별 문화행사 데이터 조회 실패");
        }
    }

    /**
     * 주어진 인구 데이터, 인구 예측 데이터 및 날씨 데이터를 업데이트하는 메소드
     * - 먼저 기존의 데이터를 모두 삭제한 후, 새 데이터를 저장한다.
     *
     * @param populationList 인구 데이터를 포함하는 목록
     * @param populationForecastList 인구 예측 데이터를 포함하는 목록
     * @param weatherList 날씨 데이터를 포함하는 목록
     */
    @Transactional
    public void updateCityData(List<Population> populationList, List<PopulationForecast> populationForecastList, List<Weather> weatherList) {
        populationForecastRepository.deleteAllInBatch();
        populationRepository.deleteAllInBatch();
        weatherRepository.deleteAllInBatch();

        populationRepository.saveAll(populationList);
        populationForecastRepository.saveAll(populationForecastList);
        weatherRepository.saveAll(weatherList);
    }
}
