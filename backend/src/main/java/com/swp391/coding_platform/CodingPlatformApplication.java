package com.swp391.coding_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class CodingPlatformApplication {

	public static void main(String[] args) {
		try {
			java.io.File envFile = new java.io.File(".env").getAbsoluteFile();
			if (envFile.exists()) {
				java.nio.file.Files.lines(envFile.toPath())
						.map(String::trim)
						.filter(line -> !line.isEmpty() && !line.startsWith("#"))
						.forEach(line -> {
							int eqIndex = line.indexOf('=');
							if (eqIndex > 0) {
								String key = line.substring(0, eqIndex).trim();
								String val = line.substring(eqIndex + 1).trim();
								System.setProperty(key, val);
							}
						});
			}
		} catch (Exception e) {
			System.err.println("Failed to load .env file: " + e.getMessage());
		}
		SpringApplication.run(CodingPlatformApplication.class, args);
	}
}
