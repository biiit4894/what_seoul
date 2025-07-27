package org.example.what_seoul.controller.area;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.area.dto.*;
import org.example.what_seoul.service.area.AreaService;
import org.example.what_seoul.swagger.operation.description.area.AreaDescription;
import org.example.what_seoul.swagger.responses.error.CommonErrorResponses;
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

    @Operation(summary = "현위치 기반 장소 리스트 조회", description = AreaDescription.GET_AREA_BY_LOCATION)
    @CommonErrorResponses
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "현위치 인근 장소 리스트 조회 성공")
    })
    @PostMapping("/location")
    public ResponseEntity<CommonResponse<ResGetAreaListByCurrentLocationDTO>> getAreaListByCurrentLocation(@RequestBody ReqGetAreaListByCurrentLocationDTO req) {
        return ResponseEntity.status(HttpStatus.OK).body(areaService.getAreaListByCurrentLocation(req));
    }

    @Operation(summary = "장소 키워드 검색", description = AreaDescription.GET_AREA_LIST_BY_KEYWORD)
    @CommonErrorResponses
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "장소 검색 성공")
    })
    @GetMapping("")
    public ResponseEntity<CommonResponse<ResGetAreaListByKeywordDTO>> getAreaListByKeyword(
            @Parameter(description = "검색 키워드", example = "회기")
            @RequestParam String query
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(areaService.getAreaListByKeyword(query));
    }

    @Operation(summary = "전체 장소 리스트 조회", description = AreaDescription.GET_ALL_AREA_LIST)
    @CommonErrorResponses
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 장소 리스트 조회 성공")
    })
    @GetMapping("/all")
    public ResponseEntity<CommonResponse<List<AreaDTO>>> getAllAreaList() {
        return ResponseEntity.status(HttpStatus.OK).body(areaService.getAllAreaList());
    }

    @Operation(summary = "전체 장소 혼잡도 조회", description = AreaDescription.GET_ALL_AREA_LIST_WITH_CONGESTION_LEVEL)
    @CommonErrorResponses
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = """
                            전체 장소 혼잡도 조회 성공\s
                            - 전체 장소 리스트가 길어 간략한 데이터 예시로 대체합니다.
                            """
            )
    })
    @GetMapping("/all/ppltn")
    public ResponseEntity<CommonResponse<List<ResGetAreaWithCongestionLevelDTO>>> getAllAreaListWithCongestionLevel() {
        return ResponseEntity.status(HttpStatus.OK).body(areaService.getAllAreasWithCongestionLevel());
    }

    @Operation(summary = "전체 장소 날씨 조회", description = AreaDescription.GET_ALL_AREA_LIST_WITH_WEATHER)
    @CommonErrorResponses
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = """
                            전체 장소 날씨 조회 성공\s
                            - 전체 장소 리스트가 길어 간략한 데이터 예시로 대체합니다.
                            """
            )
    })
    @GetMapping("/all/weather")
    public ResponseEntity<CommonResponse<List<ResGetAreaWithWeatherDTO>>> getAllAreaListWithWeather() {
        return ResponseEntity.status(HttpStatus.OK).body(areaService.getAllAreasWithWeather());
    }

    @Operation(summary = "전체 장소 문화행사 조회", description = AreaDescription.GET_ALL_AREAS_WITH_CULTURE_EVENT)
    @CommonErrorResponses
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = """
                            전체 장소 문화행사 조회 성공\s
                            - 전체 장소 리스트가 길어 간략한 데이터 예시로 대체합니다.
                            """
            )    })
    @GetMapping("/all/event")
    public ResponseEntity<CommonResponse<List<ResGetAreaWithCultureEventDTO>>> getAllAreasWithCultureEvent() {
        return ResponseEntity.status(HttpStatus.OK).body(areaService.getAllAreasWithCultureEvent());
    }

    @Operation(summary = "후기를 작성한 장소 이름 목록 조회", description = AreaDescription.GET_AREA_NAMES_WITH_MY_BOARDS)
    @CommonErrorResponses
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "후기 작성 장소 이름 목록 조회 성공") // TODO
    })
    @GetMapping("/reviewed")
    public ResponseEntity<CommonResponse<List<String>>> getAreaNamesWithMyBoards() {
        return ResponseEntity.status(HttpStatus.OK).body(areaService.getAreaNamesWithMyBoards());
    }


}
