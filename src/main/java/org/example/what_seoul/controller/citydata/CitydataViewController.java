package org.example.what_seoul.controller.citydata;

import lombok.RequiredArgsConstructor;
import org.example.what_seoul.service.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class CitydataViewController {
    private final UserService userService;

    @GetMapping("/citydata")
    public String getCitydataMainView(Model model) {
        model.addAttribute("authPrincipal", userService.getAuthenticationPrincipal());

        if (!Objects.equals(model.getAttribute("authPrincipal"), "anonymousUser")) {
            model.addAttribute("loginUserInfo", userService.getLoginUserInfo());
        }
        return "citydata/citydata";
    }
}
