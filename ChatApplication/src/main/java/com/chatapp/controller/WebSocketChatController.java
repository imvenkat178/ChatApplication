package com.chatapp.controller;

import com.chatapp.dto.ChatMessageDTO;
import com.chatapp.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class WebSocketChatController {

    private final ChatService chatService;

    public WebSocketChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDTO chatMessage) {
        // Since we disabled authentication, get sender from message payload
        // Client already sets "from" field from sessionStorage

        if (chatMessage.getTimestamp() == null) {
            chatMessage.setTimestamp(LocalDateTime.now());
        }

        System.out.println("WebSocket message: " + chatMessage.getFrom() + " -> " + chatMessage.getTo());

        chatService.sendMessage(chatMessage);
    }
}