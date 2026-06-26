package com.swp391.coding_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class CodingPlatformApplication {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void checkTables() {
        try {
            java.util.List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema='public'", String.class);
            System.out.println("====== TABLES IN DB ======");
            tables.forEach(System.out::println);
            System.out.println("==========================");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
		System.out.println("DEBUG: System property DB_URL = " + System.getProperty("DB_URL"));
		System.out.println("DEBUG: System property DB_USERNAME = " + System.getProperty("DB_USERNAME"));
		System.out.println("DEBUG: System property DB_PASSWORD = " + System.getProperty("DB_PASSWORD"));
		System.out.println("DEBUG: Env property DB_PASSWORD = " + System.getenv("DB_PASSWORD"));
		SpringApplication.run(CodingPlatformApplication.class, args);
	}
}
