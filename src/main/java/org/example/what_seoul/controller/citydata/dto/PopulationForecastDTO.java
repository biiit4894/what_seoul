package org.example.what_seoul.controller.citydata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.citydata.population.PopulationForecast;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PopulationForecastDTO {
    @Schema(description = "예측 인구 현황 데이터 ID", example = "1")
    private Long id;

    @Schema(description = """
            장소 예측 혼잡도 지표 \s
            - 지표의 종류는 아래와 같습니다. (혼잡도 오름차순)
            - 여유, 보통, 약간 붐빔, 붐빔
            """,
            example = "보통")
    private String forecastCongestionLevel;

    @Schema(description = "예측 실시간 인구 지표 최소값 (단위: 명)", example = "24000")
    private String forecastPopulationMin;

    @Schema(description = "예측 실시간 인구 지표 최대값 (단위: 명)" , example = "22000")
    private String forecastPopulationMax;

    @Schema(description = "인구 혼잡도 예측 시점", example = "2025-07-23 19:00")
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
