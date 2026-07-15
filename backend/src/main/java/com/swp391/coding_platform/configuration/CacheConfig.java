package com.swp391.coding_platform.configuration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // Spring Boot auto-configures RedisCacheManager if spring-data-redis is on classpath
    // We just need to enable caching here.
}
