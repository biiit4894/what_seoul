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
                .requestMatchers("/api/area/all", "/api/area/all/weather", "/api/area/all/ppltn", "/", "/login", "/signup", "/api/user/signup", "/api/user/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/user/list").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/user/{id}").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/user/update", "/api/user/withdraw").authenticated()
                .anyRequest().denyAll()
        ).csrf(auth -> auth.disable());
        return http.build();
    }
}
