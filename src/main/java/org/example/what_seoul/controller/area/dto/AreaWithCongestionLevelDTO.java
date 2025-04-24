package org.example.what_seoul.controller.area.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AreaWithCongestionLevelDTO {
    private Long populationId;
    private Long areaId;
    private String areaName;
    private String polygonWkt;
    private String congestionLevel;
}
