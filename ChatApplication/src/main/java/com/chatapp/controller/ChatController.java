package com.chatapp.controller;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chatapp.service.*;

@RestController()
public class ChatController {
	
	@Autowired
	private ChatService chatService;
	
	@PostMapping("chat/send")
	public String sendMessage(
	        @RequestParam String recipient,
	        @RequestParam String message) {
	    chatService.sendMessage(recipient, message);
	    return "Message sent to " + recipient + ": " + message;
	}
	
	 @RabbitListener(queues = "user1.queue") // recipient’s queue
	    public void receive(String message) {
	        System.out.println("Received message: " + message);
	    }
}