package com.cabapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot Application for Real-Time Cab Booking System
 *
 * Features:
 * - User Authentication (username/password with sessions)
 * - Real-time driver location tracking
 * - Kafka Streams for event processing
 * - Redis Geospatial for nearest driver queries
 * - WebSocket for live frontend updates
 * - Simulated drivers with automatic movement
 *
 * @author CabApp Team
 */
@SpringBootApplication
@EnableScheduling  // Enable @Scheduled tasks for driver simulation
public class CabappApplication {

    public static void main(String[] args) {
        SpringApplication.run(CabappApplication.class, args);

        System.out.println("🚖 =================================================");
        System.out.println("🚖 Cab Booking Application Started Successfully!");
        System.out.println("🚖 =================================================");
        System.out.println("📍 User authentication with sessions active");
        System.out.println("📍 Real-time driver tracking active");
        System.out.println("🔄 Kafka Streams processing location updates");
        System.out.println("📡 WebSocket endpoints ready for frontend");
        System.out.println("🗺️  Redis geospatial queries enabled");
        System.out.println("🤖 Driver simulation starting...");
        System.out.println("🚖 =================================================");
        System.out.println("🌐 Frontend: Connect React app to http://localhost:8080");
        System.out.println("🗄️  H2 Console: http://localhost:8080/h2-console");
        System.out.println("🚖 =================================================");
    }
}