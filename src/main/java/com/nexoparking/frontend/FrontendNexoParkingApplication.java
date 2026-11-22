package com.nexoparking.frontend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FrontendNexoParkingApplication {

	public static void main(String[] args) {
		SpringApplication.run(FrontendNexoParkingApplication.class, args);
	}
}
