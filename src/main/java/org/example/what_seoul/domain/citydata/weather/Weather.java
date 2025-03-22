package org.example.what_seoul.domain.citydata.weather;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.citydata.Area;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Weather {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
    기온
     */
    @Column(nullable = false)
    private String temperature;

    /*
    최고 기온
     */
    @Column(nullable = false)
    private String maxTemperature;

    /*
    최저 기온
     */
    @Column(nullable = false)
    private String minTemperature;

    /*
    초미세먼지 지표
     */
    @Column(nullable = false)
    private String pm25Index;

    /*
    미세먼지 지표
     */
    @Column(nullable = false)
    private String pm10Index;

    /*
    강수 관련 메시지
     */
    @Column(nullable = false)
    private String pcpMsg;

    /*
    날씨 데이터 업데이트 시간
     */
    @Column(nullable = false)
    private String weatherUpdateTime;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", unique = true)
    private Area area;

}
