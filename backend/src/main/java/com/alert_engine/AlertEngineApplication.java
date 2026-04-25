package com.alert_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAsync
@SpringBootApplication
public class AlertEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlertEngineApplication.class, args);
        System.out.println("Alert Engine Application started successfully!");
	}


}
