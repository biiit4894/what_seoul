package org.example.what_seoul.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityTestConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                // 누구나 접근 가능
                .requestMatchers("/api/area/all", "/api/area/all/weather", "/api/area/all/ppltn", "/", "/login", "/signup", "/api/user/signup").permitAll()
                // ADMIN 접근 가능
                .requestMatchers(HttpMethod.GET, "/api/user/list").hasRole("ADMIN")
                // ADMIN 또는 USER 접근 가능
                .requestMatchers(HttpMethod.GET, "/api/user/{id}").hasAnyRole("ADMIN", "USER")
                // 로그인 시 접근 가능
                .requestMatchers("/api/user/update", "/api/user/withdraw").authenticated()
                .anyRequest().denyAll()
        ).csrf(auth -> auth.disable());

        return http.build();
    }
}
