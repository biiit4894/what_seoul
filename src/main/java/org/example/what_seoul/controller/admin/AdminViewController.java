package org.example.what_seoul.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.service.admin.AdminService;
import org.example.what_seoul.service.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Objects;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AdminViewController {
    private final AdminService adminService;
    private final UserService userService;


    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("authPrincipal", userService.getAuthenticationPrincipal());

        if (!Objects.equals(model.getAttribute("authPrincipal"), "anonymousUser") && userService.getLoginUserInfo() != null) {
            model.addAttribute("loginUserInfo", userService.getLoginUserInfo());
        }

        return "admin/settings";
    }

    @GetMapping("/new-admin")
    public String newAdmin(Model model) {
        model.addAttribute("authPrincipal", userService.getAuthenticationPrincipal());

        if (!Objects.equals(model.getAttribute("authPrincipal"), "anonymousUser") && userService.getLoginUserInfo() != null) {
            model.addAttribute("loginUserInfo", userService.getLoginUserInfo());
        }

        return "admin/new-admin";
    }

    @GetMapping("/upload-area")
    public String showUploadPage(Model model) {
        model.addAttribute("authPrincipal", userService.getAuthenticationPrincipal());

        if (!Objects.equals(model.getAttribute("authPrincipal"), "anonymousUser") && userService.getLoginUserInfo() != null) {
            model.addAttribute("loginUserInfo", userService.getLoginUserInfo());
        }
        return "admin/upload-area";
    }
}
