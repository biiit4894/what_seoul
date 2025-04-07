package org.example.what_seoul.domain.citydata;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 서울시 주요 116 장소 (핫스팟)
 */
@Entity
@Getter
@NoArgsConstructor
public class Area {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 장소 분류
     * - 관광특구, 고궁·문화유산, 인구밀집지역, 발달상권, 공원
     */
    @Column(nullable = false)
    private String category;

    /**
     * 장소코드
     * - POI001 ~ POI116
     */
    @Column(nullable = false)
    private String areaCode;

    /**
     * 장소명
     * - ex. 강남 MICE 관광특구
     */
    @Column(nullable = false)
    private String areaName;

    /**
     * Polygon 데이터 (WKT 형식)
     */
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String polygonWkt;

    public Area(String category, String areaCode, String areaName, String polygonWkt) {
        this.category = category;
        this.areaCode = areaCode;
        this.areaName = areaName;
        this.polygonWkt = polygonWkt;
    }

}
