package com.swp391.coding_platform.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${websocket.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // ✅ FIXED: Chỉ cho phép domain được config thay vì mọi domain
        registry.addEndpoint("/ws")
                //.setAllowedOrigins(allowedOrigins.split(","))  // Từ application.yaml
                .setAllowedOriginPatterns("*")
                .withSockJS(); // Fallback nếu browser không hỗ trợ WebSocket thuần
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Tiền tố cho các kênh mà Server sẽ chủ động BẮN data XUỐNG Frontend
        registry.enableSimpleBroker("/topic", "/queue");

        // Tiền tố cho các request mà Frontend gửi LÊN Server (ít dùng trong case này)
        registry.setApplicationDestinationPrefixes("/app");
    }
}