package com.swp391.coding_platform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(com.swp391.coding_platform.TestcontainersConfiguration.class)
class CodingPlatformApplicationTests {

	static {
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
			System.err.println("Failed to load .env file in tests: " + e.getMessage());
		}
	}

	@Test
	void contextLoads() {
	}

}

