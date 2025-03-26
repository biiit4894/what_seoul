package org.example.what_seoul.controller.citydata.event.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.what_seoul.domain.citydata.event.CultureEvent;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResCultureEventDTO {
    private Long id;
    private String eventName;
    private String eventPeriod;
    private String eventPlace;
    private String eventX;
    private String eventY;
    private String thumbnail;
    private String url;
    private Long areaId;

    public ResCultureEventDTO(CultureEvent cultureEvent) {
        this.id = cultureEvent.getId();
        this.eventName = cultureEvent.getEventName();
        this.eventPeriod = cultureEvent.getEventPeriod();
        this.eventPlace = cultureEvent.getEventPlace();
        this.eventX = cultureEvent.getEventX();
        this.eventY = cultureEvent.getEventY();
        this.thumbnail = cultureEvent.getThumbnail();
        this.url = cultureEvent.getUrl();
        this.areaId = cultureEvent.getArea().getId();
    }

    public static List<ResCultureEventDTO> from(List<CultureEvent> cultureEventList) {
        return cultureEventList.stream()
                .map(ResCultureEventDTO::new)
                .collect(Collectors.toList());
    }
}
