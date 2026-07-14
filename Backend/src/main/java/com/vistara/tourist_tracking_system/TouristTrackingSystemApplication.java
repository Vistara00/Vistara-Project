package com.vistara.tourist_tracking_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TouristTrackingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(TouristTrackingSystemApplication.class, args);
        System.out.println("========================================");
        System.out.println("  VISTARA BACKEND SERVER STARTED!");
        System.out.println("  API URL: http://localhost:8087/api/v1");
        System.out.println("  WebSocket: ws://localhost:8087/ws");
        System.out.println("========================================");
	}
}
