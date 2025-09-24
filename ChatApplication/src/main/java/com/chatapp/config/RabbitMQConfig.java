package com.chatapp.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "chat.exchange";

    @Bean
    DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    // Each user will have a queue bound to the direct exchange with their username as routing key
    @Bean
    Queue userQueue() {
        return new Queue("user1.queue"); // for demonstration; in real app, dynamically create per user
    }

    @Bean
    Binding binding(Queue userQueue, DirectExchange exchange) {
        return BindingBuilder.bind(userQueue).to(exchange).with("user1"); // routing key = recipient id
    }
}