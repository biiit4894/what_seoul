package org.example.what_seoul.controller.citydata;

import lombok.AllArgsConstructor;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.citydata.dto.*;
import org.example.what_seoul.service.citydata.CitydataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citydata")
@AllArgsConstructor
public class CitydataController {
    private final CitydataService citydataService;

    @GetMapping("/population/{areaId}")
    public ResponseEntity<CommonResponse<ResPopulationDTO>> getPopulationData(@PathVariable Long areaId) {
        return ResponseEntity.ok().body(citydataService.findPopulationDataByAreaId(areaId));
    }

    @GetMapping("/weather/{areaId}")
    public ResponseEntity<CommonResponse<ResWeatherDTO>> getWeatherData(@PathVariable Long areaId) {
        return ResponseEntity.ok().body(citydataService.findWeatherDataByAreaId(areaId));
    }

    @GetMapping("/event/{areaId}")
    public ResponseEntity<CommonResponse<List<ResCultureEventDTO>>> getCultureEventData(@PathVariable Long areaId) {
        return ResponseEntity.ok().body(citydataService.findCultureEventDataByAreaId(areaId));
    }

    @PostMapping("/lbs")
    public ResponseEntity<CommonResponse<ResLocationBasedCityDataDTO>> getLocationBasedCityData(@RequestBody ReqLocationBasedCityDataDTO reqLocationBasedCityDataDTO) throws Exception {
        return ResponseEntity.ok().body(citydataService.getLocationBasedCityData(reqLocationBasedCityDataDTO));
    }
}
