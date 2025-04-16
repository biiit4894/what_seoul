package org.example.what_seoul.controller.area.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AreaWithWeatherDTO {
    private Long weatherId;
    private Long areaId;
    private String areaName;
    private String polygonWkt;
    private String temperature;
    private String pcpMsg;
}
