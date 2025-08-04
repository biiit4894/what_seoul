package org.example.what_seoul.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("WhatSeoul API Document")
                .version("1.0")
                .description("서울시내 주요 장소의 날씨, 혼잡도, 문화행사 정보를 제공하는 '왓서울'의 API 명세서입니다.");
        return new OpenAPI()
                .addServersItem(new Server().url("http://localhost:8089/"))
                .info(info);
    }
}
