package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients
@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
public class TrainDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrainDemoApplication.class, args);
	}

}
