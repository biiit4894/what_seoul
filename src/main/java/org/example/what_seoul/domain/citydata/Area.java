package org.example.what_seoul.domain.citydata;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 서울시 주요 116 장소 (핫스팟)
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
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

    /**
     * 서울시 주요 장소 정보 저장 일시
     */
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deletedAt;

    public Area(String category, String areaCode, String areaName, String polygonWkt) {
        this.category = category;
        this.areaCode = areaCode;
        this.areaName = areaName;
        this.polygonWkt = polygonWkt;
    }

    public Area(String category, String areaCode, String areaName) {
        this.category = category;
        this.areaCode = areaCode;
        this.areaName = areaName;
    }

    public void setPolygonWkt(String polygonWkt) {
        this.polygonWkt = polygonWkt;
    }

    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

    public void setDeletedAt() {
        this.deletedAt = LocalDateTime.now();
    }
}
