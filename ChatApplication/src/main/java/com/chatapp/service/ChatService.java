package com.chatapp.service;

import com.chatapp.dto.ChatMessageDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.chatapp.config.RabbitMQConfig;

@Service
public class ChatService {

    private final RabbitTemplate rabbitTemplate;

    public ChatService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(String recipient, ChatMessageDTO message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, recipient, message);
    }
}