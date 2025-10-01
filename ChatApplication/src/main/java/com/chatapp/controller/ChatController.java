package com.chatapp.controller;

import com.chatapp.dto.ChatMessageDTO;
import com.chatapp.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody ChatMessageDTO messageDTO, Principal principal) {
        // Get authenticated user from Basic Auth
        String authenticatedUser = principal.getName();

        // Override "from" field to prevent spoofing
        messageDTO.setFrom(authenticatedUser);

        if (messageDTO.getTimestamp() == null) {
            messageDTO.setTimestamp(LocalDateTime.now());
        }

        chatService.sendMessage(messageDTO);

        return ResponseEntity.ok().body(new MessageResponse("Message sent successfully"));
    }

    static class MessageResponse {
        private String status;

        public MessageResponse(String status) {
            this.status = status;
        }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}