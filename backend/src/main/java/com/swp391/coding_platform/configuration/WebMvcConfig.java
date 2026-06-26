package com.swp391.coding_platform.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        java.io.File rootDir = new java.io.File(".").getAbsoluteFile();
        java.io.File backendDir = new java.io.File(rootDir, "backend");
        java.io.File baseDir = backendDir.exists() && backendDir.isDirectory() ? backendDir : rootDir;
        java.io.File uploadDir = new java.io.File(baseDir, "uploads");

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadDir.toURI().toString());
    }
}
