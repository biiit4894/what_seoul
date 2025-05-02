package org.example.what_seoul.domain.citydata.event;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.citydata.Area;

import java.time.LocalDateTime;

/**
 * 문화행사 현황
 */
@Entity
@Getter
@NoArgsConstructor
public class CultureEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 문화행사명
     */
    @Column(nullable = false)
    private String eventName;

    /**
     * 문화행사 기간
     */
    @Column(nullable = false)
    private String eventPeriod;

    /**
     * 문화행사 장소
     */
    @Column(nullable = false)
    private String eventPlace;

    /**
     * 문화행사 X 좌표(경도)
     */
    @Column(nullable = false)
    private String eventX;

    /**
     * 문화행사 Y 좌표(위도)
     */
    @Column(nullable = false)
    private String eventY;

    /**
     * 문화행사 대표 이미지
     */
    @Column(nullable = false)
    private String thumbnail;

    /**
     * 문화행사 상세정보 URL
     */
    @Column(nullable = false)
    private String url;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    private Area area;

    public CultureEvent(String eventName, String eventPeriod, String eventPlace, String eventX, String eventY, String thumbnail, String url, Area area) {
        this.eventName = eventName;
        this.eventPeriod = eventPeriod;
        this.eventPlace = eventPlace;
        this.eventX = eventX;
        this.eventY = eventY;
        this.thumbnail = thumbnail;
        this.url = url;
        this.createdAt = LocalDateTime.now();
        this.area = area;
    }

}
