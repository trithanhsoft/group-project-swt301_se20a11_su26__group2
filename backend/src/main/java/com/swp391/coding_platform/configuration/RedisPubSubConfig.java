package com.swp391.coding_platform.configuration;

import com.swp391.coding_platform.listener.RedisContestEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisPubSubConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisContestEventListener contestEventListener) {
        
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        // Đăng ký listener lắng nghe channel "contest:events"
        container.addMessageListener(
                contestEventListener, 
                new ChannelTopic("contest:events")
        );
        
        return container;
    }
}
