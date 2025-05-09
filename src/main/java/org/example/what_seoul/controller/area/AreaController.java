package org.example.what_seoul.controller.area;

import lombok.AllArgsConstructor;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.area.dto.*;
import org.example.what_seoul.service.area.AreaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/area")
@AllArgsConstructor
public class AreaController {
    private final AreaService areaService;

    @PostMapping("/location")
    public ResponseEntity<CommonResponse<ResGetAreaListByCurrentLocationDTO>> getAreaListByCurrentLocation(@RequestBody ReqGetAreaListByCurrentLocationDTO req) {
        return ResponseEntity.status(HttpStatus.OK).body(areaService.getAreaListByCurrentLocation(req));
    }

    @GetMapping("")
    public ResponseEntity<CommonResponse<ResGetAreaListByKeywordDTO>> getAreaListByKeyword(@RequestParam String query) {
        return ResponseEntity.status(HttpStatus.OK).body(areaService.getAreaListByKeyword(query));
    }

    @GetMapping("/all")
    public ResponseEntity<CommonResponse<List<AreaDTO>>> getAllAreaList() {
        return ResponseEntity.status(HttpStatus.OK).body(areaService.getAllAreaList());
    }

    @GetMapping("/all/ppltn")
    public ResponseEntity<CommonResponse<List<ResGetAreaWithCongestionLevelDTO>>> getAllAreaListWithCongestionLevel() {
        return ResponseEntity.status(HttpStatus.OK).body(areaService.getAllAreasWithCongestionLevel());
    }

    @GetMapping("/all/weather")
    public ResponseEntity<CommonResponse<List<ResGetAreaWithWeatherDTO>>> getAllAreaListWithWeather() {
        return ResponseEntity.status(HttpStatus.OK).body(areaService.getAllAreasWithWeather());
    }

    @GetMapping("/all/event")
    public ResponseEntity<CommonResponse<List<ResGetAreaWithCultureEventDTO>>> getAllAreasWithCultureEvent() {
        return ResponseEntity.status(HttpStatus.OK).body(areaService.getAllAreasWithCultureEvent());
    }


}
