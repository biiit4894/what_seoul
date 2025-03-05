package org.example.what_seoul.controller;

import lombok.AllArgsConstructor;
import org.example.what_seoul.common.dto.CommonResponse;
import org.example.what_seoul.dto.CreateUserReq;
import org.example.what_seoul.dto.CreateUserRes;
import org.example.what_seoul.dto.GetUserDetailRes;
import org.example.what_seoul.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<CreateUserRes>> signup(@RequestBody CreateUserReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<GetUserDetailRes>> getUserDetail(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserDetailById(id));
    }

}
