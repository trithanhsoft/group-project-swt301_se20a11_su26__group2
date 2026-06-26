package com.swp391.coding_platform.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

@Configuration
public class PayOSConfig {

    @Bean
    public PayOS payOS(ProjectProperties.Payos payosProps) {
        return new PayOS(payosProps.getClientId(), payosProps.getApiKey(), payosProps.getChecksumKey());
    }
}
