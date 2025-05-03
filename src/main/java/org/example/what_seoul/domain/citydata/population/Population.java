package org.example.what_seoul.domain.citydata.population;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.citydata.Area;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 실시간 인구현황
 */
@Entity
@Getter
@NoArgsConstructor
public class Population {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 장소 혼잡도 지표
     */
    @Column(nullable = false)
    private String congestionLevel;

    /**
     * 장소 혼잡도 지표 관련 메시지
     */
    @Column(nullable = false)
    private String congestionMessage;

    /**
     * 실시간 인구 지표 최소값
     */
    @Column(nullable = false)
    private String populationMin;

    /**
     * 실시간 인구 지표 최대값
     */
    @Column(nullable = false)
    private String populationMax;

    /**
     * 실시간 인구 데이터 업데이트 시간
     */
    @Column(nullable = false)
    private String populationUpdateTime;

    /**
     * 실시간 인구 현황 데이터 저장 일시
     */
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 예측 인구 데이터(인구 예측값)
     */
    @OneToMany(mappedBy = "population", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PopulationForecast> forecasts = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    public Population(String congestionLevel, String congestionMessage, String populationMin, String populationMax, String populationUpdateTime, Area area) {
        this.congestionLevel = congestionLevel;
        this.congestionMessage = congestionMessage;
        this.populationMin = populationMin;
        this.populationMax = populationMax;
        this.populationUpdateTime = populationUpdateTime;
        this.createdAt = LocalDateTime.now();
        this.area = area;
    }
}
