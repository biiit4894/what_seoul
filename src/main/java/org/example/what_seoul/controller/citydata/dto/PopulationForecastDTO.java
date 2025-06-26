package org.example.what_seoul.controller.citydata.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.citydata.population.PopulationForecast;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PopulationForecastDTO {
    private Long id;
    private String forecastCongestionLevel;
    private String forecastPopulationMin;
    private String forecastPopulationMax;
    private String forecastTime;

    public PopulationForecastDTO(PopulationForecast populationForecast) {
        this.id = populationForecast.getId();
        this.forecastCongestionLevel = populationForecast.getForecastCongestionLevel();
        this.forecastPopulationMin = populationForecast.getForecastPopulationMin();
        this.forecastPopulationMax = populationForecast.getForecastPopulationMax();
        this.forecastTime = populationForecast.getForecastTime();
    }
    public static PopulationForecastDTO from(PopulationForecast populationForecast) {
        return new PopulationForecastDTO(populationForecast);
    }
}
