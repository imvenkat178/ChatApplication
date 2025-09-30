package com.chatapp.controller;

import com.chatapp.model.User;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chatapp.dto.ChatMessageDTO;
import com.chatapp.service.*;

@RestController()
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("chat/send")
    public String sendMessage(
            @RequestBody ChatMessageDTO message) {
        chatService.sendMessage(message);
        return "Message sent to " + message.getTo() + ": " + message;
    }
}