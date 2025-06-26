package org.example.what_seoul.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String errorMessage = "로그인에 실패했습니다"; // 기본적인 오류 메시지
        if (exception instanceof UsernameNotFoundException) {
            errorMessage = "존재하지 않는 아이디입니다.";
        } else if (exception instanceof BadCredentialsException) {
            errorMessage = "아이디 또는 비밀번호가 맞지 않습니다.";
        }

        // 실패 메시지를 리다이렉트할 때 파라미터로 전달
        request.getSession().setAttribute("errorMessage", errorMessage);

        // 리다이렉트 시에 에러가 발생했음을 파라미터에 명시하며 로그인 페이지로 이동
        response.sendRedirect("/login?error=true");
    }
}

