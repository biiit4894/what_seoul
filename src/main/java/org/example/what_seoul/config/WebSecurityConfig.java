package org.example.what_seoul.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.what_seoul.repository.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
@Slf4j
@Profile("dev")
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity, CustomAuthenticationFailureHandler failureHandler) throws Exception {
        httpSecurity
                .csrf(auth -> auth.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, userRepository), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(auth -> auth
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // 누구나 접근 가능
                        .requestMatchers("/css/**", "/js/**", "/media/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(
                                "/", "/login", "/signup", "/findId", "/findPw",
                                "/api/user/login", "/api/user/signup", "/api/user/find/id", "/api/user/find/pw",
                                "/api/admin/login",
                                "/api/auth/access/reissue",
                                "/actuator/health"
                        ).permitAll()
                        // ADMIN만 접근 가능
                        .requestMatchers(HttpMethod.GET, "/settings").hasRole("ADMIN") // 서비스 관리 및 설정 페이지
                        .requestMatchers(HttpMethod.GET, "/new-admin").hasRole("ADMIN") // 관리자 계정 생성 페이지
                        .requestMatchers(HttpMethod.GET, "/upload-area").hasRole("ADMIN") // 서울시 주요 장소 등록 페이지
                        .requestMatchers(HttpMethod.GET, "/areas").hasRole("ADMIN") // 서울시 주요 장소 목록 조회 페이지
                        .requestMatchers(HttpMethod.GET, "/api/user/list").hasRole("ADMIN") // 회원 목록 조회 기능
                        .requestMatchers(HttpMethod.POST, "/api/admin/signup").hasRole("ADMIN") // 관리자 계정 생성 기능
                        .requestMatchers(HttpMethod.POST, "/api/admin/area/list").hasRole("ADMIN") // 장소 정보 목록 조회 기능
                        .requestMatchers(HttpMethod.POST, "/api/admin/area").hasRole("ADMIN") // 장소 정보 업로드 기능
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/area").hasRole("ADMIN") // 장소 정보 삭제 기능
                        // 로그인 시 접근 가능
                        .requestMatchers("/citydata/**").authenticated()
                        .requestMatchers("/mypage").authenticated()
                        .requestMatchers("/access-denied").authenticated()
                        .requestMatchers("/api/user/**").authenticated()
                        .requestMatchers("/api/area/**").authenticated()
                        .requestMatchers("/api/citydata/**").authenticated()
                        .requestMatchers("/api/board/**").authenticated()
                        .requestMatchers("/api/auth/logout").authenticated()
                        .anyRequest().denyAll())
                .logout(AbstractHttpConfigurer::disable); // 시큐리티 로그아웃 기능 비활성화(logout.disable())
        return httpSecurity.build();
    }

    @Bean
    @Primary
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
