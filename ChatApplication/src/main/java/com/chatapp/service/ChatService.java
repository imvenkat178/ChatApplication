package com.chatapp.service;

import com.chatapp.dto.ChatMessageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final RabbitTemplate rabbitTemplate;
    private final DirectExchange chatExchange;
    private final ObjectMapper objectMapper;

    public ChatService(RabbitTemplate rabbitTemplate, DirectExchange chatExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.chatExchange = chatExchange;

        // Configure ObjectMapper to handle LocalDateTime
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void sendMessage(ChatMessageDTO chatMessage) {
        try {
            // Convert DTO to JSON string
            String messageJson = objectMapper.writeValueAsString(chatMessage);

            // Publish to RabbitMQ
            // Routing key = recipient username
            rabbitTemplate.convertAndSend(
                    chatExchange.getName(),
                    chatMessage.getTo(),
                    messageJson
            );

            System.out.println("Message sent to RabbitMQ: " + messageJson);

        } catch (JsonProcessingException e) {
            System.err.println("Error serializing message: " + e.getMessage());
            throw new RuntimeException("Failed to send message", e);
        }
    }
}