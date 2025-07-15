package org.example.what_seoul.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.admin.dto.*;
import org.example.what_seoul.service.admin.AdminService;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin API", description = "관리자 전용 기능입니다.")
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/signup")
    @Operation(
            summary = "관리자 계정 생성",
            description = """
                    관리자 계정을 생성합니다. \s
                    - accessToken 쿠키를 통해 관리자 권한을 확인합니다. \s
                    - userId, email, nickName 중복 및 DTO 유효성 검사를 수행합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "관리자 계정 생성 성공")
    })
    public ResponseEntity<CommonResponse<ResCreateAdminDTO>> signup(
            @Parameter(description = "Access Token (쿠키)", required = true)
            @CookieValue("accessToken") String accessToken,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "관리자 계정 생성 요청 DTO(userId, password, email, nickName)",
                    required = true
            )
            @RequestBody ReqCreateAdminDTO req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createAdminUser(accessToken, req));
    }

    @PostMapping("/login")
    @Operation(
            summary = "관리자 로그인",
            description = """
                    관리자 계정으로 로그인합니다. \s
                    - 로그인 성공 시 AccessToken, RefreshToken을 HttpOnly 쿠키로 전달합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "관리자 로그인 성공")
    })
    public ResponseEntity<CommonResponse<ResAdminLoginDTO>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "로그인 요청 DTO (userId, password)",
                    required = true
            )
            @RequestBody ReqAdminLoginDTO req,

            HttpServletResponse response
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(adminService.login(req, response));
    }

    @PostMapping("/area/list")
    @Operation(
            summary = "서울시 주요 장소 목록 조회",
            description = """
                    서울시 주요 장소 목록을 페이지 단위로 조회합니다. \s
                    - 검색어(`areaName`)는 요청 바디로 전달됩니다 (선택값). \s
                    - 결과는 Slice 형태로 반환됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "장소 목록 조회 성공")
    })
    public ResponseEntity<CommonResponse<Slice<ResGetAreaListDTO>>> getAreaList(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(name = "size", defaultValue = "10") int size,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "검색어를 포함한 요청 DTO (선택)",
                    required = false,
                    content = @Content(schema = @Schema(implementation = ReqGetAreaListDTO.class))
            )
            @RequestBody(required = false) ReqGetAreaListDTO req
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(adminService.getAreaList(page, size, req));
    }

    @PostMapping("/area")
    @Operation(
            summary = "서울시 주요 장소 정보 업로드",
            description = """
                    관리자가 .zip 형식의 Shapefile 데이터를 업로드하면, \s
                    Python 스크립트를 통해 GeoJSON으로 변환 후 장소 정보를 저장합니다. \s
                    저장된 항목 수, 수정된 항목 수, 중복으로 건너뛴 항목 수 등을 응답합니다.\s
                    - 서울시 공공데이터 API에서 제공하는 서울시 주요 장소 정보가 변경되는 경우,\s
                    - 해당 변경 사항에 맞추어 WhatSeoul 서비스의 장소 데이터를 갱신하기 위한 기능입니다.\s
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "업로드 및 저장 성공")
    })
    public ResponseEntity<CommonResponse<ResUploadAreaDTO>> uploadArea(
            @Parameter(
                    description = "Shapefile .zip 형식의 업로드 파일",
                    required = true,
                    schema = @Schema(type = "string", format = "binary"),
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestParam("file") MultipartFile multipartFile
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.processAreaFile(multipartFile));
    }

    @DeleteMapping("/area")
    @Operation(
            summary = "서울시 주요 장소 삭제",
            description = """
                전달받은 ID 목록에 해당하는 장소들을 삭제 처리합니다. \s
                실제로는 deletedAt을 업데이트하는 soft delete 방식입니다.\s
                
                - 서울시 공공데이터 API에서, 서울시 주요 장소 목록 중 일부 장소를 제거하여 더 이상 실시간 도시데이터를 제공하지 않는 경우,\s
                - 해당 변경 사항에 맞추어 WhatSeoul의 장소 데이터를 삭제 처리하기 위한 기능입니다.\s
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 처리 성공")
    })
    public ResponseEntity<CommonResponse<List<ResDeleteAreaDTO>>> deleteAreas(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "삭제할 장소 ID 리스트",
                    required = true,
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(
                                            type = "integer",
                                            example = "[1, 2, 3]"
                                    )
                            )
                    )
            )
            @RequestBody List<Long> ids
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(adminService.deleteArea(ids));
    }
}
