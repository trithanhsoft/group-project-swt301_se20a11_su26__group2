package com.swp391.coding_platform.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String CONTEST_RANKING_DB_UPDATE_QUEUE = "contest.ranking.db.update";

    @Bean
    public Queue contestRankingDbUpdateQueue() {
        // Queue bền vững (durable = true) để chống mất dữ liệu khi restart broker
        return new Queue(CONTEST_RANKING_DB_UPDATE_QUEUE, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
