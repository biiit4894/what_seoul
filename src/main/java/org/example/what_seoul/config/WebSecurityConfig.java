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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

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
    public WebSecurityCustomizer configure() {      // 스프링 시큐리티 기능 비활성화
        return web -> web.ignoring().requestMatchers("/static/**","/css/**", "/js/**", "/media/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity, CustomAuthenticationFailureHandler failureHandler) throws Exception {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setMatchingRequestParameterName(null);
        httpSecurity
                .csrf(auth -> auth.disable())
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, userRepository), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(auth -> auth
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // 누구나 접근 가능
                        .requestMatchers(
                                "/", "/login", "/signup", "/findId", "/findPw",
                                "/api/user/signup", "/api/user/find/id", "/api/user/find/pw",
                                "/api/admin/login"
                        ).permitAll()
                        // ADMIN만 접근 가능
                        .requestMatchers(HttpMethod.GET, "/api/user/list").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/admin/signup").hasRole("ADMIN")
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

                            if (isApiRequest(request)) {
                                response.setContentType("application/json");
                                response.setCharacterEncoding("UTF-8");
                            } else {
                                response.sendRedirect("/");
                            }
                        }))
                .logout(auth -> auth.logoutSuccessUrl("/")
                        .invalidateHttpSession(true));
        return httpSecurity.build();
    }

    @Bean
    @Primary
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/api/admin");
    }
}
