package com.swp391.coding_platform.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration properties for the project.
 * These classes help the IDE (Antigravity/VS Code) recognize custom properties in application.yaml
 * and provide autocomplete/validation.
 */
public class ProjectProperties {

    @Configuration
    @ConfigurationProperties(prefix = "judge0")
    @Data
    public static class Judge0 {
        private String baseUrl;
        private Duration timeout = Duration.ofSeconds(20);
    }

    @Configuration
    @ConfigurationProperties(prefix = "websocket")
    @Data
    public static class Websocket {
        private String allowedOrigins;
    }

    @Configuration
    @ConfigurationProperties(prefix = "app")
    @Data
    public static class App {
        private String webhookBaseUrl;
    }

    @Configuration
    @ConfigurationProperties(prefix = "payos")
    @Data
    public static class Payos {
        private String clientId;
        private String apiKey;
        private String checksumKey;
        private String returnUrl;
        private String cancelUrl;
    }
}
