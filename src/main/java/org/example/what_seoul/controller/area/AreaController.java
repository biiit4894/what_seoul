package org.example.what_seoul.controller.area;

import lombok.AllArgsConstructor;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.area.dto.ReqGetAreaListByCurrentLocationDTO;
import org.example.what_seoul.controller.area.dto.ResGetAreaListByKeywordDTO;
import org.example.what_seoul.controller.area.dto.ResGetAreaListByCurrentLocationDTO;
import org.example.what_seoul.service.area.AreaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/area")
@AllArgsConstructor
public class AreaController {
    private final AreaService areaService;

    @PostMapping("/location")
    public ResponseEntity<CommonResponse<ResGetAreaListByCurrentLocationDTO>> getAreaListByCurrentLocation(@RequestBody ReqGetAreaListByCurrentLocationDTO reqGetAreaListByCurrentLocationDTO) {
        return ResponseEntity.ok().body(areaService.getLocationBasedCityData(reqGetAreaListByCurrentLocationDTO));
    }

    @GetMapping("")
    public ResponseEntity<CommonResponse<ResGetAreaListByKeywordDTO>> getAreaListByKeyword(@RequestParam String query) {
        return ResponseEntity.ok().body(areaService.getAreaListByKeyword(query));
    }
}
