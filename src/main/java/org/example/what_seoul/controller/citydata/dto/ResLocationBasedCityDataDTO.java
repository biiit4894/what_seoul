package org.example.what_seoul.controller.citydata.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResLocationBasedCityDataDTO {
    private List<PlaceDTO> nearestPlaces;
}
