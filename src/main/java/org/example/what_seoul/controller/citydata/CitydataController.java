package org.example.what_seoul.controller.citydata;

import lombok.AllArgsConstructor;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.citydata.dto.*;
import org.example.what_seoul.service.citydata.CitydataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/citydata")
@AllArgsConstructor
public class CitydataController {
    private final CitydataService citydataService;

    @GetMapping("/population/{areaId}")
    public ResponseEntity<CommonResponse<ResGetPopulationDataDTO>> getPopulationData(@PathVariable Long areaId) {
        return ResponseEntity.status(HttpStatus.OK).body(citydataService.findPopulationDataByAreaId(areaId));
    }

    @GetMapping("/weather/{areaId}")
    public ResponseEntity<CommonResponse<ResGetWeatherDataDTO>> getWeatherData(@PathVariable Long areaId) {
        return ResponseEntity.status(HttpStatus.OK).body(citydataService.findWeatherDataByAreaId(areaId));
    }

    @GetMapping("/event/{areaId}")
    public ResponseEntity<CommonResponse<List<ResGetCultureEventDataDTO>>> getCultureEventData(@PathVariable Long areaId) {
        return ResponseEntity.status(HttpStatus.OK).body(citydataService.findCultureEventDataByAreaId(areaId));
    }
}
