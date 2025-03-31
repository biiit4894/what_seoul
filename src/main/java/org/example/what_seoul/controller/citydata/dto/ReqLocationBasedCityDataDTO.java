package org.example.what_seoul.controller.citydata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReqLocationBasedCityDataDTO {
    private double latitude;
    private double longitude;
}
