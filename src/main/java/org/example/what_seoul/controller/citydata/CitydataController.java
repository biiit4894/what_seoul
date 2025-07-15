package org.example.what_seoul.controller.citydata;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.citydata.dto.*;
import org.example.what_seoul.service.citydata.CitydataService;
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

    @Operation(
            summary = "인구 현황 데이터 조회",
            description = "특정 장소(areaId)에 대한 실시간 인구 현황 데이터를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(
                    schema = @Schema(implementation = ResGetPopulationDataDTO.class),
                    examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "message": "장소별 인구 현황 데이터 조회 성공",
                              "data": {
                                "id": 1,
                                "congestionLevel": "보통",
                                "congestionMessage": "사람이 적당히 있어요.",
                                "populationMin": "120",
                                "populationMax": "250",
                                "populationUpdateTime": "2025-07-15 10:00",
                                "areaId": 101,
                                "forecasts": [
                                  {
                                    "forecastTime": "11:00",
                                    "forecastLevel": "혼잡",
                                    "forecastMin": "200",
                                    "forecastMax": "300"
                                  }
                                ]
                              },
                              "responseTime": "2025-07-15T12:00:00.000Z"
                            }
                            """)
            ))
    })
    @GetMapping("/population/{areaId}")
    public ResponseEntity<CommonResponse<ResGetPopulationDataDTO>> getPopulationData(@PathVariable Long areaId) {
        return ResponseEntity.status(HttpStatus.OK).body(citydataService.findPopulationDataByAreaId(areaId));
    }

    @Operation(
            summary = "날씨 현황 데이터 조회",
            description = "특정 장소(areaId)에 대한 날씨 데이터를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(
                    schema = @Schema(implementation = ResGetWeatherDataDTO.class),
                    examples = @ExampleObject(value = """
                    {
                      "success": true,
                      "message": "장소별 날씨 현황 데이터 조회 성공",
                      "data": {
                        "id": 5,
                        "temperature": "23.1",
                        "maxTemperature": "27.0",
                        "minTemperature": "19.5",
                        "pm25Index": "보통",
                        "pm25": "20",
                        "pm10Index": "좋음",
                        "pm10": "15",
                        "pcpMsg": "비가 오지 않아요",
                        "weatherUpdateTime": "2025-07-15 09:00",
                        "areaId": 101
                      },
                      "responseTime": "2025-07-15T12:00:00.000Z"
                    }
                    """)
            ))
    })
    @GetMapping("/weather/{areaId}")
    public ResponseEntity<CommonResponse<ResGetWeatherDataDTO>> getWeatherData(@PathVariable Long areaId) {
        return ResponseEntity.status(HttpStatus.OK).body(citydataService.findWeatherDataByAreaId(areaId));
    }

    @Operation(
            summary = "문화행사 목록 조회",
            description = "해당 장소에서 열리는 문화행사 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = ResGetCultureEventDataDTO.class)),
                    examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "message": "장소별 문화행사 데이터 조회 성공",
                              "data": [
                                {
                                  "id": 1,
                                  "eventName": "2025 서울 재즈 페스티벌",
                                  "eventPeriod": "2025.07.20 ~ 2025.07.22",
                                  "eventPlace": "올림픽공원",
                                  "eventX": "127.12345",
                                  "eventY": "37.54321",
                                  "thumbnail": "https://example.com/image1.jpg",
                                  "url": "https://festival.seoul.kr",
                                  "isEnded": false,
                                  "areaId": 100
                                }
                              ],
                              "responseTime": "2025-07-15T12:00:00.000Z"
                            }
                        """)
            ))
    })
    @GetMapping("/event/{areaId}")
    public ResponseEntity<CommonResponse<List<ResGetCultureEventDataDTO>>> getCultureEventData(@PathVariable Long areaId) {
        return ResponseEntity.status(HttpStatus.OK).body(citydataService.findCultureEventDataByAreaId(areaId));
    }
}
