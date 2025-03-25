package org.example.what_seoul.domain.citydata.population;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 인구 예측값 (향후 12시간에 대한 인구 예측 현황)
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopulationForecast {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 장소 예측 혼잡도 지표
     */
    @Column(nullable = false)
    private String forecastCongestionLevel;

    /**
     * 예측 실시간 인구 지표 최소값
     */
    @Column(nullable = false)
    private String forecastPopulationMin;

    /**
     * 예측 실시간 인구 지표 최대값
     */
    @Column(nullable = false)
    private String forecastPopulationMax;

    /**
     * 인구 혼잡도 예측 시점
     */
    @Column(nullable = false)
    private String forecastTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "population_id", nullable = false)
    private Population population;

    public PopulationForecast(String forecastCongestionLevel, String forecastPopulationMin, String forecastPopulationMax, String forecastTime, Population population) {
        this.forecastCongestionLevel = forecastCongestionLevel;
        this.forecastPopulationMin = forecastPopulationMin;
        this.forecastPopulationMax = forecastPopulationMax;
        this.forecastTime = forecastTime;
        this.population = population;
    }
}
