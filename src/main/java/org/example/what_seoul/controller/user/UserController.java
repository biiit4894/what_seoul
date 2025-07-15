package org.example.what_seoul.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.user.dto.*;
import org.example.what_seoul.service.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User API", description = "사용자 회원가입, 로그인, 회원정보 수정 및 찾기, 회원정보 조회 관련 기능입니다.")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "새로운 일반 사용자 계정을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원 가입 성공", content = @Content(
                    schema = @Schema(implementation = ResCreateUserDTO.class),
                    examples = @ExampleObject(value = """
                {
                  "success": true,
                  "message": "회원 가입 성공",
                  "data": {
                    "id": 1,
                    "userId": "testuser",
                    "email": "test@example.com",
                    "nickName": "테스트"
                  },
                  "responseTime": "2025-07-15T13:00:00.000Z"
                }
            """)
            ))
    })
    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<ResCreateUserDTO>> signup(@RequestBody ReqCreateUserDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(req));
    }

    @Operation(summary = "로그인", description = "아이디와 비밀번호를 이용해 일반 사용자 로그인을 수행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(
                    schema = @Schema(implementation = ResUserLoginDTO.class),
                    examples = @ExampleObject(value = """
                {
                  "success": true,
                  "message": "회원 로그인 성공",
                  "data": {
                    "userId": "testuser",
                    "accessTokenExpiresAt": "2025-07-15T14:00:00"
                  },
                  "responseTime": "2025-07-15T13:00:00.000Z"
                }
            """)
            ))
    })
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<ResUserLoginDTO>> login(@RequestBody ReqUserLoginDTO req, HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.login(req, response));
    }

    @Operation(summary = "회원 리스트 조회", description = "전체 회원의 요약 정보를 페이징하여 조회합니다.")
    @GetMapping("/list")
    public ResponseEntity<CommonResponse<Page<ResGetUserDetailSummaryDTO>>> getUserList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserList(page, size));
    }

    @Operation(summary = "회원 상세 조회", description = "회원 ID로 특정 사용자의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 상세 조회 성공"),
    })
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<ResGetUserDetailDTO>> getUserDetail(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserDetailById(id));
    }

    @Operation(summary = "회원 정보 수정", description = "로그인한 사용자의 비밀번호, 이메일, 닉네임을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 정보 수정 성공"),
    })
    @PutMapping("/update")
    public ResponseEntity<CommonResponse<ResUpdateUserDTO>> updateUserInfo(@RequestBody ReqUpdateUserInfoDTO req) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserInfo(req));
    }

    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 계정을 탈퇴 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
    })
    @PutMapping("/withdraw")
    public ResponseEntity<CommonResponse<ResWithdrawUserDTO>> withdrawUser(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.withdrawUser(request, response));
    }

    @Operation(summary = "아이디 찾기", description = "이메일로 사용자 아이디를 조회하고 메일로 전송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "아이디 찾기 성공")
    })
    @PostMapping("/find/id")
    public ResponseEntity<CommonResponse<Void>> findUserId(@RequestBody ReqFindUserIdDTO req) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findUserIdByEmail(req));
    }

    @Operation(summary = "비밀번호 초기화", description = "임시 비밀번호를 이메일로 전송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 찾기 성공")
    })
    @PostMapping("/find/pw")
    public ResponseEntity<CommonResponse<Void>> findPassword(@RequestBody ReqFindPasswordDTO req) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.resetPassword(req));
    }
}
