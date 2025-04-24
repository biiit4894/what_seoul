package org.example.what_seoul.controller.user;

import lombok.RequiredArgsConstructor;
import org.example.what_seoul.service.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class UserViewController {
    private final UserService userService;

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("authPrincipal", userService.getAuthenticationPrincipal());
        return "user/login";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("authPrincipal", userService.getAuthenticationPrincipal());
        return "user/signup";
    }

    @GetMapping("/mypage")
    public String mypage(Model model) {
        model.addAttribute("authPrincipal", userService.getAuthenticationPrincipal());

        if (!Objects.equals(model.getAttribute("authPrincipal"), "anonymousUser")) {
            model.addAttribute("loginUserInfo", userService.getLoginUserInfo());
        }

        return "user/mypage";
    }
}
