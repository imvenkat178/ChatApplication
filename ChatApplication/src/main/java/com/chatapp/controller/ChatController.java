package com.chatapp.controller;

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
        chatService.sendMessage(message.getTo(), message);
        return "Message sent to " + message.getTo() + ": " + message;
    }

    @RabbitListener(queues = "user1.queue")
    public void receive(ChatMessageDTO chatMessage) {
        System.out.println("Message for User1 from "
                + chatMessage.getFrom() + ": " + chatMessage.getMessage());
    }
}