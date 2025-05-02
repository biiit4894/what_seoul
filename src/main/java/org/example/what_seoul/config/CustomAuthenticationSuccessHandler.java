package org.example.what_seoul.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // 로그인 성공 후 메시지를 RedirectAttributes에 추가
        RedirectAttributes redirectAttributes = (RedirectAttributes) request.getAttribute("redirectAttributes");
        if (redirectAttributes != null) {
            redirectAttributes.addFlashAttribute("loginSuccessMessage", "로그인에 성공하였습니다.");
        }

        // 홈 페이지로 리다이렉트
        response.sendRedirect("/");
    }
}
