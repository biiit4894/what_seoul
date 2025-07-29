package org.example.what_seoul.controller.area.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CoordinateDTO {
    @Schema(description = "좌표의 위도", example = "37.590266389129916")
    private double lat;

    @Schema(description = "좌표의 경도", example = "127.05414215214367")
    private double lon;
}
