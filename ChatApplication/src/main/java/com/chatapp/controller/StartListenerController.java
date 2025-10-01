package com.chatapp.controller;

import com.chatapp.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class StartListenerController {

    private final UserService userService;

    public StartListenerController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/start-listener")
    public void startListener(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (username != null) {
            System.out.println("Starting listener for: " + username);
            userService.startListenerForUser(username);
        }
    }
}