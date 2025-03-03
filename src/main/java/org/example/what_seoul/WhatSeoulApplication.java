package org.example.what_seoul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class WhatSeoulApplication {

	public static void main(String[] args) {
		SpringApplication.run(WhatSeoulApplication.class, args);
	}

}
