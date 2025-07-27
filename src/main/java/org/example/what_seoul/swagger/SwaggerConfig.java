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
                .version("1.0");
        return new OpenAPI()
                .addServersItem(new Server().url("http://localhost:8089"))
                .info(info);
    }
}
