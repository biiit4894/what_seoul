package org.example.what_seoul.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@Configuration
@EnableWebSecurity
@Profile("test-h2")
public class WebSecurityTestWithH2Config {
    @Bean(name = "configureForTestWithH2Config")
    public WebSecurityCustomizer configure() {      // 스프링 시큐리티 기능 비활성화
        return web -> web.ignoring().requestMatchers(toH2Console());
    }

    @Bean(name = "securityFilterChainForTestWithH2Config")
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
                .anyRequest().denyAll()
        ).csrf(auth -> auth.disable());

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
