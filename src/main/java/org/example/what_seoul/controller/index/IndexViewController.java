package org.example.what_seoul.controller.index;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.service.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Objects;

@Controller
@RequiredArgsConstructor
@Slf4j
public class IndexViewController {
    private final UserService userService;
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("authPrincipal", userService.getAuthenticationPrincipal());
//        model.addAttribute("loginUserInfo", userService.getLoginUserInfo());

        if (!Objects.equals(model.getAttribute("authPrincipal"), "anonymousUser")) {
            log.info("not anonymous");
            model.addAttribute("loginUserInfo", userService.getLoginUserInfo());
        }
        return "index";
    }
}
