package com.pucp.dp1.grupo4d.morapack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MorapackApplication {

	public static void main(String[] args) {
		SpringApplication.run(MorapackApplication.class, args);
	}

}
