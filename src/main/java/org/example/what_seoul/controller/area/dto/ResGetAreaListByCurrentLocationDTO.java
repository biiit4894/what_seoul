package org.example.what_seoul.controller.area.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResGetAreaListByCurrentLocationDTO {
    @Schema(description = "서울시 주요 장소 목록")
    private List<AreaDTO> nearestPlaces;
}
