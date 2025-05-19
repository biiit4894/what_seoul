package org.example.what_seoul.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
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
@Profile("dev")
public class WebSecurityConfig {
    @Bean
    public WebSecurityCustomizer configure() {      // 스프링 시큐리티 기능 비활성화
        return web -> web.ignoring().requestMatchers("/static/**","/css/**", "/js/**", "/media/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity, CustomAuthenticationFailureHandler failureHandler) throws Exception {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setMatchingRequestParameterName(null);
        httpSecurity.authorizeHttpRequests(auth -> auth
                // 누구나 접근 가능
                .requestMatchers("/", "/login", "/signup", "/findId", "/findPw", "/api/user/signup", "/api/user/find/id").permitAll()
                // ADMIN만 접근 가능
                .requestMatchers(HttpMethod.GET, "/api/user/list").hasRole("ADMIN")
                // 로그인 시 접근 가능
                .requestMatchers("/api/user/**").authenticated()
                .requestMatchers("/api/area/**").authenticated()
                .requestMatchers("/api/citydata/**").authenticated()
                .requestMatchers("/api/board/**").authenticated()
                .requestMatchers("/citydata/**").authenticated()
                .requestMatchers("/mypage").authenticated()
                .anyRequest().denyAll())
                .formLogin(auth -> auth
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .failureHandler(failureHandler)
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
    @Primary
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
