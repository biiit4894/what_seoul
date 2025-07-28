package org.example.what_seoul.config;

import org.example.what_seoul.repository.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@Configuration
@EnableWebSecurity
@Profile("test-h2")
public class WebSecurityTestWithH2Config {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public WebSecurityTestWithH2Config(JwtTokenProvider jwtTokenProvider, UserRepository userRepository, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint, JwtAccessDeniedHandler jwtAccessDeniedHandler) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    @Bean(name = "configureForTestWithH2Config")
    public WebSecurityCustomizer configure() {      // 스프링 시큐리티 기능 비활성화
        return web -> web.ignoring().requestMatchers(toH2Console());
    }

    @Bean(name = "securityFilterChainForTestWithH2Config")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(auth -> auth.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, userRepository), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(auth -> auth
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                // 누구나 접근 가능
                .requestMatchers(
                        "/api/user/login", "/api/user/signup", "/api/user/find/id", "/api/user/find/pw",
                        "/api/admin/login",
                        "/api/auth/access/reissue"
                ).permitAll()
                // ADMIN만 접근 가능
                .requestMatchers(HttpMethod.GET, "/api/user/list").hasRole("ADMIN") // 회원 목록 조회 기능
                .requestMatchers(HttpMethod.POST, "/api/admin/signup").hasRole("ADMIN") // 관리자 계정 생성 기능
                // 로그인 시 접근 가능
                .requestMatchers("/api/user/**").authenticated()
                .requestMatchers("/api/area/**").authenticated()
                .requestMatchers("/api/citydata/**").authenticated()
                .requestMatchers("/api/board/**").authenticated()
                .requestMatchers("/api/auth/logout").authenticated()
                .anyRequest().denyAll()
        ).csrf(auth -> auth.disable());

        return http.build();
    }

    @Bean(name = "bCryptPasswordEncoderForTestWithH2Config")
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
