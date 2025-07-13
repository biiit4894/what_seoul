package org.example.what_seoul.controller.admin;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.admin.dto.*;
import org.example.what_seoul.service.admin.AdminService;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<ResCreateAdminDTO>> signup(
            @CookieValue("accessToken") String accessToken,
            @RequestBody ReqCreateAdminDTO req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createAdminUser(accessToken, req));
    }

    @PostMapping("/login")
    public ResponseEntity<CommonResponse<ResAdminLoginDTO>> login(@RequestBody ReqAdminLoginDTO req, HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.OK).body(adminService.login(req, response));
    }

    @PostMapping("/area/list")
    public ResponseEntity<CommonResponse<Slice<ResGetAreaListDTO>>> getAreaList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestBody(required = false) ReqGetAreaListDTO req
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(adminService.getAreaList(page, size, req));
    }

    @PostMapping("/area")
    public ResponseEntity<CommonResponse<ResUploadAreaDTO>> uploadArea(@RequestParam("file") MultipartFile multipartFile) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.processAreaFile(multipartFile));
    }

    @DeleteMapping("/area")
    public ResponseEntity<CommonResponse<List<ResDeleteAreaDTO>>> deleteAreas(@RequestBody List<Long> ids) {
        return ResponseEntity.status(HttpStatus.OK).body(adminService.deleteArea(ids));
    }
}
