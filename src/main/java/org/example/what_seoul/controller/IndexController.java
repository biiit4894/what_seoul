package org.example.what_seoul.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view/index")
@RequiredArgsConstructor
public class IndexController {
    @GetMapping("")
    public String getIndex() {
        return "index";
    }
}
