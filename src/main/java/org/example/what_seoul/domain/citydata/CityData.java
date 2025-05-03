package org.example.what_seoul.domain.citydata;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.what_seoul.domain.citydata.event.CultureEvent;
import org.example.what_seoul.domain.citydata.population.Population;
import org.example.what_seoul.domain.citydata.population.PopulationForecast;
import org.example.what_seoul.domain.citydata.weather.Weather;

import java.util.List;

/**
 * 유형별 도시데이터를 하나로 모아 저장하기 위한 용도의 클래스
 * - 인구 현황 데이터(인구 예측값 포함), 날씨 현황 데이터, 문화행사 데이터를 저장한다.
 */
@RequiredArgsConstructor
@Getter
public class CityData {
    private final Population population;
    private final List<PopulationForecast> populationForecast;
    private final Weather weather;
    private final List<CultureEvent> cultureEvent;
}
