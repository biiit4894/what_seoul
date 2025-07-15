package org.example.what_seoul.controller.area;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.area.dto.*;
import org.example.what_seoul.service.area.AreaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/area")
@RequiredArgsConstructor
@Tag(name = "Area API", description = "실시간 도시데이터를 제공하는 서울시 주요 장소 관련 기능입니다.")
public class AreaController {
    private final AreaService areaService;

    @Operation(
            summary = "현위치 기반 장소 리스트 조회",
            description = """
                    유저의 현위치 위도와 경도를 기준으로 가장 가까운 서울시 주요 장소 리스트를 조회합니다. \s
                    - GPS 좌표를 기반으로 인접 장소들을 반환합니다.\s
                    - 유저 위치를 포함하는 장소가 있다면, 해당 장소를 한 곳을 반환합니다.\s
                    - 유저 위치를 포함하는 장소가 없다면, 가장 가까운 3개 장소를 반환합니다.\s
                    - 반환되는 장소는 거리순으로 정렬됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(
                                    name = "SuccessExample",
                                    summary = "성공 응답 예시",
                                    value = """
                                              {
                                                "success": true,
                                                "message": "현위치 기반 장소 리스트 조회 성공",
                                                "data": {
                                                "nearestPlaces": [
                                                  {
                                                    "id": 14,
                                                    "areaName": "강남역",
                                                    "polygonCoords": [
                                                      { "lat": 37.590266, "lon": 127.05414 },
                                                      { "lat": 37.5903, "lon": 127.0542 }
                                                    ]
                                                  }
                                                 
                                                ]
                                              },
                                              "responseTime": "2025-07-15T12:24:51"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/location")
    public ResponseEntity<CommonResponse<ResGetAreaListByCurrentLocationDTO>> getAreaListByCurrentLocation(@RequestBody ReqGetAreaListByCurrentLocationDTO req) {
        return ResponseEntity.status(HttpStatus.OK).body(areaService.getAreaListByCurrentLocation(req));
    }

    @Operation(
            summary = "장소 키워드 검색",
            description = """
                    입력된 키워드에 해당하는 서울시 주요 장소들을 반환합니다. \s
                    - 삭제처리 되지 않은 장소만 검색됩니다. \s
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "검색 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(
                                    name = "SuccessExample",
                                    summary = "성공 응답 예시",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "장소 검색 성공",
                                              "data": [
                                                                
                                                {
                                                  "id": 1,
                                                  "areaName": "강남 MICE 관광특구",
                                                  "polygonCoords": [
                                                    { "lat": 37.554576, "lon": 127.083998 },
                                                    { "lat": 37.5546, "lon": 127.084 }
                                                  ]
                                                },
                                                {
                                                  "id": 14,
                                                  "areaName": "강남역",
                                                  "polygonCoords": [
                                                    { "lat": 37.590266, "lon": 127.054142 },
                                                    { "lat": 37.5903, "lon": 127.0542 }
                                                  ]
                                                }
                                              ],
                                              "responseTime": "2025-07-15T13:45:00"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("")
    public ResponseEntity<CommonResponse<ResGetAreaListByKeywordDTO>> getAreaListByKeyword(
            @Parameter(description = "검색 키워드", example = "강남"
            )
            @RequestParam String query
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(areaService.getAreaListByKeyword(query));
    }

    @Operation(
            summary = "전체 장소 리스트 조회",
            description = """
                    서울시 주요 장소 전체 목록을 List 형태로 반환합니다.
                    - 삭제처리 되지 않은 모든 장소를 조회합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "전체 장소 리스트 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(
                                    name = "SuccessExample",
                                    summary = "성공 응답 예시",
                                    description =  "전체 장소 목록이 길어 간략히 기재합니다",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "전체 장소 리스트 조회 성공",
                                              "data": [
                                                                
                                                {
                                                  "id": 1,
                                                  "areaName": "강남 MICE 관광특구",
                                                  "polygonCoords": [
                                                    { "lat": 37.554576, "lon": 127.083998 },
                                                    { "lat": 37.5546, "lon": 127.084 }
                                                  ]
                                                },
                                                {
                                                  "id": 2,
                                                  "areaName": "동대문 관광특구",
                                                  "polygonCoords": [
                                                    { "lat": 37.590266, "lon": 127.054142 },
                                                    { "lat": 37.5903, "lon": 127.0542 }
                                                  ]
                                                }
                                              ],
                                              "responseTime": "2025-07-15T13:45:00"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/all")
    public ResponseEntity<CommonResponse<List<AreaDTO>>> getAllAreaList() {
        return ResponseEntity.status(HttpStatus.OK).body(areaService.getAllAreaList());
    }

    @Operation(
            summary = "전체 장소 혼잡도 조회",
            description = """
                        실시간 혼잡도 정보가 포함된 서울시 주요 장소 전체 리스트를 반환합니다.
                        - 삭제처리 되지 않은 모든 장소에 한하여 조회합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "전체 장소 혼잡도 조회 성공",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = CommonResponse.class),
                        examples = @ExampleObject(
                                name = "SuccessExample",
                                summary = "성공 응답 예시",
                                description =  "전체 장소 목록이 길어 간략히 기재합니다",
                                value = """
                                        {
                                          "success": true,
                                          "message": "전체 장소 혼잡도 조회 성공",
                                          "data": [
                                                            
                                            {
                                              "id": 1,
                                              "areaName": "강남 MICE 관광특구",
                                              "polygonCoords": [
                                                { "lat": 37.554576, "lon": 127.083998 },
                                                { "lat": 37.5546, "lon": 127.084 }
                                              ],
                                              "congestionLevel": "보통"
                                            },
                                            {
                                              "id": 2,
                                              "areaName": "동대문 관광특구",
                                              "polygonCoords": [
                                                { "lat": 37.590266, "lon": 127.054142 },
                                                { "lat": 37.5903, "lon": 127.0542 }
                                              ],
                                              "congestionLevel": "약간 붐빔"
                                            }
                                          ],
                                          "responseTime": "2025-07-15T13:45:00"
                                        }
                                        """
                        )
                    )
            )
    })
    @GetMapping("/all/ppltn")
    public ResponseEntity<CommonResponse<List<ResGetAreaWithCongestionLevelDTO>>> getAllAreaListWithCongestionLevel() {
        return ResponseEntity.status(HttpStatus.OK).body(areaService.getAllAreasWithCongestionLevel());
    }

    @Operation(
            summary = "전체 장소 날씨 조회",
            description = """
                        실시간 날씨 정보가 포함된 서울시 주요 장소 전체 리스트를 반환합니다.
                        - 삭제처리 되지 않은 모든 장소에 한하여 조회합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "전체 장소 날씨 조회 성공",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = CommonResponse.class),
                        examples = @ExampleObject(
                                name = "SuccessExample",
                                summary = "성공 응답 예시",
                                description =  "전체 장소 목록이 길어 간략히 기재합니다",
                                value = """
                                    {
                                      "success": true,
                                      "message": "전체 장소 날씨 조회 성공",
                                      "data": [

                                        {
                                          "areaId": 1,
                                          "areaName": "강남 MICE 관광특구",
                                          "polygonCoords": [
                                            { "lat": 37.554576, "lon": 127.083998 },
                                            { "lat": 37.5546, "lon": 127.084 }
                                          ],
                                          "temperature": "26.1",
                                          "pcpMsg": "약한 비가 내리고 있어요.외출 시 우산을 챙기세요."
                                        },
                                        {
                                          "areaId": 2,
                                          "areaName": "동대문 관광특구",
                                          "polygonCoords": [
                                            { "lat": 37.590266, "lon": 127.054142 },
                                            { "lat": 37.5903, "lon": 127.0542 }
                                          ],
                                         
                                          "temperature": "25.7",
                                          "pcpMsg": "비 또는 눈 소식이 없어요."
                                        }
                                      ],
                                      "responseTime": "2025-07-15T13:45:00"
                                    }
                                    """
                        )
                    )
            )
    })
    @GetMapping("/all/weather")
    public ResponseEntity<CommonResponse<List<ResGetAreaWithWeatherDTO>>> getAllAreaListWithWeather() {
        return ResponseEntity.status(HttpStatus.OK).body(areaService.getAllAreasWithWeather());
    }

    @Operation(
            summary = "전체 장소 문화행사 조회",
            description = """
                        문화행사 정보가 포함된 서울시 주요 장소 전체 리스트를 반환합니다.
                        - 삭제처리 되지 않은 모든 장소에 한하여 조회합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "전체 장소 문화행사 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(
                                    name = "SuccessExample",
                                    summary = "성공 응답 예시",
                                    description =  "전체 장소 목록이 길어 간략히 기재합니다",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "전체 장소 문화행사 조회 성공",
                                              "data": [
                                                {
                                                  "areaId": 2,
                                                  "areaName": "동대문 관광특구",
                                                  "polygonCoords": [
                                                    { "lat": 37.544, "lon": 127.036 },
                                                    { "lat": 37.545, "lon": 127.037 }
                                                  ],
                                                  "cultureEventList": [
                                                       {
                                                           "cultureEventId": 3594,
                                                           "eventName": "[DDP] 2025 DDP 봄축제 [어린이투어]",
                                                           "eventPeriod": "2025-05-03~2025-07-31",
                                                           "eventPlace": "DDP 전역",
                                                           "eventX": "127.00977973484339",
                                                           "eventY": "37.56735731522952",
                                                           "thumbnail": "https://culture.seoul.go.kr/cmmn/file/getImage.do?atchFileId=d80fc1e9a01a42d48878e8fc9b7b1e12&thumb=Y",
                                                           "url": "https://culture.seoul.go.kr/culture/culture/cultureEvent/view.do?cultcode=153369&menuNo=200011",
                                                           "isEnded": false
                                                       }
                                                  ]
                                                }
                                              ],
                                              "responseTime": "2025-07-15T14:00:00"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/all/event")
    public ResponseEntity<CommonResponse<List<ResGetAreaWithCultureEventDTO>>> getAllAreasWithCultureEvent() {
        return ResponseEntity.status(HttpStatus.OK).body(areaService.getAllAreasWithCultureEvent());
    }

    @Operation(
            summary = "후기를 작성한 장소 이름 목록 조회",
            description = """
                    로그인한 사용자가 작성한 후기들을 기반으로,\s
                    사용자가 후기를 작성한 이력이 있는 장소 이름 목록을 조회합니다.
                    - 삭제 처리된 장소를 포함하여 모두 조회합니다.
                    - 반환되는 장소명은 중복 제거된 리스트입니다.
                    - 마이페이지의 작성한 후기 목록 조회 화면에서, 장소명을 선택하여 조건부로 후기를 조회하기 위해 사용하는 기능입니다.
                                
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "후기 작성 장소 이름 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(
                                    name = "SuccessExample",
                                    summary = "성공 응답 예시",
                                    value = """
                                              {
                                                "success": true,
                                                "message": "후기를 작성한 장소 이름 목록 조회 성공",
                                                "data": [
                                                    "서울숲",
                                                    "한강공원",
                                                    "광화문광장"
                                                ],
                                                "responseTime": "2025-07-15T14:45:00"
                                              }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/reviewed")
    public ResponseEntity<CommonResponse<List<String>>> getAreaNamesWithMyBoards() {
        return ResponseEntity.status(HttpStatus.OK).body(areaService.getAreaNamesWithMyBoards());
    }


}
