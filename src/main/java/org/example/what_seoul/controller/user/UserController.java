package org.example.what_seoul.controller.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.controller.user.dto.*;
import org.example.what_seoul.service.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<ResCreateUserDTO>> signup(@RequestBody ReqCreateUserDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(req));
    }

    @GetMapping("/list")
    public ResponseEntity<CommonResponse<Page<ResGetUserDetailSummaryDTO>>> getUserList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserList(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<ResGetUserDetailDTO>> getUserDetail(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserDetailById(id));
    }

    @PutMapping("/update")
    public ResponseEntity<CommonResponse<ResUpdateUserDTO>> updateUserInfo(@RequestBody ReqUpdateUserInfoDTO req) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserInfo(req));
    }

    @PutMapping("/withdraw")
    public ResponseEntity<CommonResponse<ResWithdrawUserDTO>> withdrawUser(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.withdrawUser(request, response));
    }
}
