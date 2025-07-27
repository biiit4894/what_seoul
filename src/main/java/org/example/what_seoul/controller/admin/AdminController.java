package org.example.what_seoul.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.admin.dto.*;
import org.example.what_seoul.service.admin.AdminService;
import org.example.what_seoul.swagger.operation.description.admin.AdminDescription;
import org.example.what_seoul.swagger.responses.error.AdminAccessDeniedResponses;
import org.example.what_seoul.swagger.responses.error.CommonErrorResponses;
import org.example.what_seoul.swagger.responses.error.ValidationErrorResponses;
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
    @Operation(summary = "관리자 계정 생성", description = AdminDescription.SIGNUP)
    @CommonErrorResponses
    @ValidationErrorResponses
    @AdminAccessDeniedResponses
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "관리자 계정 생성 성공")
    })
    public ResponseEntity<CommonResponse<ResCreateAdminDTO>> signup(
            @Parameter(description = "Access Token (쿠키)", required = true)
            @CookieValue("accessToken") String accessToken,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "관리자 계정 생성 요청 DTO(userId, password, email, nickName)", required = true)
            @RequestBody ReqCreateAdminDTO req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createAdminUser(accessToken, req));
    }

    @PostMapping("/login")
    @Operation(summary = "관리자 로그인", description = AdminDescription.LOGIN)
    @CommonErrorResponses
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "관리자 로그인 성공")
    })
    public ResponseEntity<CommonResponse<ResAdminLoginDTO>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "로그인 요청 DTO (userId, password)", required = true)
            @RequestBody ReqAdminLoginDTO req,
            HttpServletResponse response
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(adminService.login(req, response));
    }

    @PostMapping("/area/list")
    @Operation(summary = "서울시 주요 장소 목록 조회", description = AdminDescription.GET_AREA_LIST)
    @CommonErrorResponses
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "장소 목록 조회 성공")
    })
    public ResponseEntity<CommonResponse<Slice<ResGetAreaListDTO>>> getAreaList(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(name = "size", defaultValue = "10") int size,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "검색어를 포함한 요청 DTO (선택)", content = @Content(schema = @Schema(implementation = ReqGetAreaListDTO.class)))
            @RequestBody(required = false) ReqGetAreaListDTO req
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(adminService.getAreaList(page, size, req));
    }

    @PostMapping("/area")
    @Operation(summary = "서울시 주요 장소 정보 업로드", description = AdminDescription.UPLOAD_AREA)
    @CommonErrorResponses
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
    @Operation(summary = "서울시 주요 장소 삭제", description = AdminDescription.DELETE_AREAS)
    @CommonErrorResponses
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
                                            example = "1"
                                    )
                            ),
                            examples = @ExampleObject(
                                    name = "장소 ID 리스트",
                                    summary = "삭제할 장소 ID들의 배열",
                                    value = "[1, 2, 3]"
                            )
                    )
            )
            @RequestBody List<Long> ids
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(adminService.deleteArea(ids));
    }
}
