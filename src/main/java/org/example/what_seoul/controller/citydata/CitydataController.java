package org.example.what_seoul.controller.citydata;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.citydata.dto.*;
import org.example.what_seoul.service.citydata.CitydataService;
import org.example.what_seoul.swagger.operation.description.citydata.CitydataDescription;
import org.example.what_seoul.swagger.responses.error.CommonErrorResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citydata")
@RequiredArgsConstructor
@Tag(name = "Citydata API", description = "서울시 도시데이터를 조회하는 기능입니다.")
public class CitydataController {
    private final CitydataService citydataService;

    @Operation(summary = "장소별 인구 현황 데이터 조회", description = "특정 장소에 대한 실시간 인구 현황 데이터를 조회합니다.")
    @CommonErrorResponses
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = CitydataDescription.GET_POPULATION_DATA_SUCCESS)
    })
    @GetMapping("/population/{areaId}")
    public ResponseEntity<CommonResponse<ResGetPopulationDataDTO>> getPopulationData(@PathVariable Long areaId) {
        return ResponseEntity.status(HttpStatus.OK).body(citydataService.findPopulationDataByAreaId(areaId));
    }

    @Operation(summary = "장소별 날씨 현황 데이터 조회", description = "특정 장소에 대한 실시간 날씨 현황 데이터를 조회합니다.")
    @CommonErrorResponses
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = CitydataDescription.GET_WEATHER_DATA_SUCCESS)
    })
    @GetMapping("/weather/{areaId}")
    public ResponseEntity<CommonResponse<ResGetWeatherDataDTO>> getWeatherData(@PathVariable Long areaId) {
        return ResponseEntity.status(HttpStatus.OK).body(citydataService.findWeatherDataByAreaId(areaId));
    }

    @Operation(summary = "장소별 문화행사 목록 조회", description = "특정 장소에서 열리는 문화행사 목록을 조회합니다.")
    @CommonErrorResponses
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = CitydataDescription.GET_CULTURE_EVENT_DATA_SUCCESS)
    })
    @GetMapping("/event/{areaId}")
    public ResponseEntity<CommonResponse<List<ResGetCultureEventDataDTO>>> getCultureEventData(@PathVariable Long areaId) {
        return ResponseEntity.status(HttpStatus.OK).body(citydataService.findCultureEventDataByAreaId(areaId));
    }
}
