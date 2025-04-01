package org.example.what_seoul.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@EnableWebSecurity
@Configuration
@Slf4j
public class WebSecurityConfig {
    @Bean
    public WebSecurityCustomizer configure() {      // 스프링 시큐리티 기능 비활성화
        return web -> web.ignoring().requestMatchers(toH2Console())
                .requestMatchers("/static/**","/css/**", "/js/**", "/media/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setMatchingRequestParameterName(null);
        httpSecurity.authorizeHttpRequests(auth ->
                        auth.requestMatchers(
                                        "/", "/login", "/signup", "/api/user/signup", "/api/user/{id}")
                                .permitAll()
                                .anyRequest()
                                .authenticated())
                .formLogin(auth -> auth
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .successHandler((request, response, authentication) -> {
                            log.info("로그인 성공: 사용자 {}", authentication.getName());
                            response.sendRedirect("/");

                        }))
                .logout(auth -> auth.logoutSuccessUrl("/")
                        .invalidateHttpSession(true))
                .csrf(auth -> auth.disable());
        return httpSecurity.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
