package org.example.what_seoul.controller.citydata.population.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.citydata.population.Population;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResPopulationDTO {
    private Long id;
    private String congestionLevel;
    private String congestionMessage;
    private String populationMin;
    private String populationMax;
    private String populationUpdateTime;
    private Long areaId;
    private List<ResPopulationForecastDTO> forecasts;

    public ResPopulationDTO(Population population) {
        this.id = population.getId();
        this.congestionLevel = population.getCongestionLevel();
        this.congestionMessage = population.getCongestionMessage();
        this.populationMin = population.getPopulationMin();
        this.populationMax = population.getPopulationMax();
        this.populationUpdateTime = population.getPopulationUpdateTime();
        this.areaId = population.getArea().getId();
        this.forecasts = population.getForecasts().stream()
                .map(ResPopulationForecastDTO::from)
                .collect(Collectors.toList());
    }
    public static ResPopulationDTO from(Population population) {
        return new ResPopulationDTO(population);
    }
}
