package com.chatapp.service;

import com.chatapp.dto.ChatMessageDTO;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final RabbitTemplate rabbitTemplate;
    private final DirectExchange chatExchange;

    public ChatService(RabbitTemplate rabbitTemplate, DirectExchange chatExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.chatExchange = chatExchange;
    }

    public void sendMessage(ChatMessageDTO chatMessage) {
        // Send the OBJECT directly, not JSON string
        // RabbitTemplate will serialize it automatically
        rabbitTemplate.convertAndSend(
                chatExchange.getName(),
                chatMessage.getTo(),
                chatMessage  // Send object, not JSON string
        );

        System.out.println("Message sent to RabbitMQ: " + chatMessage.getFrom() + " -> " + chatMessage.getTo());
    }
}