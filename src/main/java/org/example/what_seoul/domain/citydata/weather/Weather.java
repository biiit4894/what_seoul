package org.example.what_seoul.domain.citydata.weather;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.citydata.Area;

/**
 * 날씨 현황
 */
@Entity
@Getter
@NoArgsConstructor
public class Weather {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 기온
     */
    @Column(nullable = false)
    private String temperature;

    /**
     * 최고 기온
     */
    @Column(nullable = false)
    private String maxTemperature;

    /**
     * 최저 기온
     */
    @Column(nullable = false)
    private String minTemperature;

    /**
     * 초미세먼지 지표
     */
    @Column(nullable = false)
    private String pm25Index;

    /**
     * 초미세먼지농도
     */
    @Column(nullable = false)
    private String pm25;

    /**
     * 미세먼지 지표
     */
    @Column(nullable = false)
    private String pm10Index;

    /**
     * 미세먼지농도
     */
    @Column(nullable = false)
    private String pm10;

    /**
     * 강수 관련 메시지
     */
    @Column(nullable = false)
    private String pcpMsg;

    /**
     * 날씨 데이터 업데이트 시간
     */
    @Column(nullable = false)
    private String weatherUpdateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    public Weather(String temperature, String maxTemperature, String minTemperature, String pm25Index, String pm25, String pm10Index, String pm10, String pcpMsg, String weatherUpdateTime, Area area) {
        this.temperature = temperature;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
        this.pm25Index = pm25Index;
        this.pm25 = pm25;
        this.pm10Index = pm10Index;
        this.pm10 = pm10;
        this.pcpMsg = pcpMsg;
        this.weatherUpdateTime = weatherUpdateTime;
        this.area = area;
    }
}
