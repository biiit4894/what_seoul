package org.example.what_seoul.controller.area;

import lombok.AllArgsConstructor;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.area.dto.ReqLocationBasedCityDataDTO;
import org.example.what_seoul.controller.area.dto.ResGetAreaDTO;
import org.example.what_seoul.controller.area.dto.ResLocationBasedCityDataDTO;
import org.example.what_seoul.service.area.AreaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/area")
@AllArgsConstructor
public class AreaController {
    private final AreaService areaService;

    @PostMapping("/location")
    public ResponseEntity<CommonResponse<ResLocationBasedCityDataDTO>> getLocationBasedCityData(@RequestBody ReqLocationBasedCityDataDTO reqLocationBasedCityDataDTO) {
        return ResponseEntity.ok().body(areaService.getLocationBasedCityData(reqLocationBasedCityDataDTO));
    }

    @PostMapping("")
    public ResponseEntity<CommonResponse<ResGetAreaDTO>> getAreaByKeyword(@RequestParam String query) {
        return ResponseEntity.ok().body(areaService.getAreaByKeyword(query));
    }
}
