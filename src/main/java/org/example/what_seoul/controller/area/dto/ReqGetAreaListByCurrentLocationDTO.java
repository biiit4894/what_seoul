package org.example.what_seoul.controller.area.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReqGetAreaListByCurrentLocationDTO {
    @Schema(description = "나의 현위치 위도", example = "37.554")
    private double latitude;

    @Schema(description = "나의 현위치 위도", example = "126.971")
    private double longitude;
}
