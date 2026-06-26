package com.swp391.coding_platform.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModerationQueueConfig {

    public static final String MODERATION_QUEUE = "course.moderation.queue";
    public static final String MODERATION_EXCHANGE = "course.moderation.exchange";
    public static final String MODERATION_ROUTING_KEY = "course.submitted";

    public static final String MODERATION_DLQ = "course.moderation.dlq";
    public static final String MODERATION_DLX = "course.moderation.dlx";
    public static final String MODERATION_DLQ_ROUTING_KEY = "course.moderation.dead";

    @Bean
    public Queue moderationQueue() {
        return QueueBuilder.durable(MODERATION_QUEUE)
                .withArgument("x-dead-letter-exchange", MODERATION_DLX)
                .withArgument("x-dead-letter-routing-key", MODERATION_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(MODERATION_DLQ).build();
    }

    @Bean
    public DirectExchange moderationExchange() {
        return new DirectExchange(MODERATION_EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(MODERATION_DLX);
    }

    @Bean
    public Binding bindingModeration(
            @org.springframework.beans.factory.annotation.Qualifier("moderationQueue") Queue moderationQueue, 
            @org.springframework.beans.factory.annotation.Qualifier("moderationExchange") DirectExchange moderationExchange) {
        return BindingBuilder.bind(moderationQueue).to(moderationExchange).with(MODERATION_ROUTING_KEY);
    }

    @Bean
    public Binding bindingDLQ(
            @org.springframework.beans.factory.annotation.Qualifier("deadLetterQueue") Queue deadLetterQueue, 
            @org.springframework.beans.factory.annotation.Qualifier("deadLetterExchange") DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(MODERATION_DLQ_ROUTING_KEY);
    }

    /**
     * Listener container factory riêng cho moderation queue.
     * defaultRequeueRejected=false: message bị reject sẽ vào DLQ thay vì requeue vô hạn.
     */
    @Bean("moderationContainerFactory")
    public SimpleRabbitListenerContainerFactory moderationContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setDefaultRequeueRejected(false); // Không requeue khi listener fail → vào DLQ
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}
