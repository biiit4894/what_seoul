package org.example.what_seoul.controller.area.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CultureEventDTO {
    private Long cultureEventId;
    private String eventName;
    private String eventPeriod;
    private String eventPlace;
    private String eventX;
    private String eventY;
    private String thumbnail;
    private String url;
}
