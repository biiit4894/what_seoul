package org.example.what_seoul.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@Profile("test")
public class WebSecurityTestConfig {
    @Bean(name = "securityFilterChainForTest")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                // 누구나 접근 가능
                .requestMatchers("/", "/login", "/signup", "/api/user/signup", "/api/user/find/id", "/api/user/find/pw").permitAll()
                // ADMIN만 접근 가능
                .requestMatchers(HttpMethod.GET, "/api/user/list").hasRole("ADMIN")
                // 로그인 시 접근 가능
                .requestMatchers("/api/user/**").authenticated()
                .requestMatchers("/api/area/**").authenticated()
                .requestMatchers("/api/citydata/**").authenticated()
                .requestMatchers("/api/board/**").authenticated()
                .anyRequest().denyAll()
        ).csrf(auth -> auth.disable());

        return http.build();
    }

    @Bean(name = "bCryptPasswordEncoderForTest")
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
