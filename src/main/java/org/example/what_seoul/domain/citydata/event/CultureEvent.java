package org.example.what_seoul.domain.citydata.event;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.domain.citydata.Area;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 문화행사 현황
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@Slf4j
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

    /**
     * 문화행사 종료 여부
     */
    @Column(nullable = false)
    private Boolean isEnded;

    /**
     * 문화행사 데이터 저장 일시
     */
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 문화행사 데이터 갱신 일시
     * - 기존 행사와 동일한 행사 이름, 관련 장소(Area)를 가진 문화행사 데이터를 fetch한 경우,
     * - 1) 행사 기간, 2) 행사 장소, 3) 행사 X/Y 좌표, 4) 썸네일, 5) url 필드 중 하나라도 값이 변경된 것이 확인된다면
     * - 이 필드 값을 갱신한다.
     */
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    public CultureEvent(String eventName, String eventPeriod, String eventPlace, String eventX, String eventY, String thumbnail, String url, Area area) {
        this.eventName = eventName;
        this.eventPeriod = eventPeriod;
        this.eventPlace = eventPlace;
        this.eventX = eventX;
        this.eventY = eventY;
        this.thumbnail = thumbnail;
        this.url = url;
        this.isEnded = false;
        this.area = area;
    }

    /**
     * 행사 이름과 관련 Area가 같은 행사일 경우 정보를 갱신하는 메소드
     * - 1) 행사 기간, 2) 행사 장소, 3) 행사 X/Y 좌표, 4) 썸네일, 5) url 필드의 값이 변경되었다면 갱신한다
     * - 갱신된 시점은 updatedAt 필드에 반영한다.
     * - 아직 종료되었다고 판단되지 않은 행사라면(isEnded 필드의 값이 아직 false라면) 행사 종료 여부를 다시 판단하여 isEnded 값을 갱신한다.
     * @param cultureEvent 도시데이터 API 호출에 의해 새롭게 fetch된 문화행사 정보
     * @return 5개 필드 중 하나라도 수정되었다면 true 반환 / 하나도 수정되지 않았다면 false 반환
     */
    public boolean updateFrom(CultureEvent cultureEvent) {
        boolean isUpdated = false;
        if(!Objects.equals(this.eventPeriod, cultureEvent.getEventPeriod())) {
            log.info("[변경] {}의 eventPeriod: '{}' → '{}'", this.eventName, this.eventPeriod, cultureEvent.getEventPeriod());
            this.eventPeriod = cultureEvent.getEventPeriod();
            isUpdated = true;
        }
        if (!Objects.equals(this.eventPlace, cultureEvent.getEventPlace())) {
            log.info("[변경] {}의 eventPlace: '{}' → '{}'", this.eventName, this.eventPlace, cultureEvent.getEventPlace());
            this.eventPlace = cultureEvent.getEventPlace();
            isUpdated = true;
        }
        if (!Objects.equals(this.eventX, cultureEvent.getEventX())) {
            this.eventX = cultureEvent.getEventX();
            log.info("[변경] {}의 eventX: '{}' → '{}'", this.eventName, this.eventX, cultureEvent.getEventX());
            isUpdated = true;
        }
        if (!Objects.equals(this.eventY, cultureEvent.getEventY())) {
            this.eventY = cultureEvent.getEventY();
            log.info("[변경] {}의 eventY: '{}' → '{}'", this.eventName, this.eventY, cultureEvent.getEventY());
            isUpdated = true;
        }
        if (!Objects.equals(this.thumbnail, cultureEvent.getThumbnail())) {
            this.thumbnail = cultureEvent.getThumbnail();
            log.info("[변경] {}의 thumbnail: '{}' → '{}'", this.eventName, this.thumbnail, cultureEvent.getThumbnail());
            isUpdated = true;
        }
        if (!Objects.equals(this.url, cultureEvent.getUrl())) {
            this.url = cultureEvent.getUrl();
            log.info("[변경] {}의 url: '{}' → '{}'", this.eventName, this.url, cultureEvent.getUrl());
            isUpdated = true;
        }
        if (isUpdated) {
            this.updatedAt = LocalDateTime.now();
            log.info("id: {}, name: {} is updated", cultureEvent.getId(), cultureEvent.getEventName());
        }

        return isUpdated;
    }

    /**
     * 문화행사의 종료 여부를 갱신하는 메소드
     * - evaluateIsEnded에 의해 판단된 종료 여부(true/false)를 바탕으로 isEnded 필드 값을 갱신한다.
     * @param isEnded 종료된 경우 true / 진행중인 경우 false
     */
    public boolean updateIsEnded(boolean isEnded) {
        if (this.isEnded != isEnded) {
            this.isEnded = isEnded;
            return true;
        }
        return false;
    }

    /**
     * 문화행사의 종료 여부를 판단하는 메소드
     * @return 종료된 경우 true / 진행중인 경우 false
     */
    public boolean evaluateIsEnded() {
        try {
            String[] dates = this.eventPeriod.split("~");
            LocalDate endDate = LocalDate.parse(dates[1].trim());
            return endDate.isBefore(LocalDate.now());
        } catch (Exception e) {
            log.warn("Failed to parse eventPeriod: {}", this.eventPeriod);
            return false; // 파싱 실패 시 기본값은 false
        }
    }
}
