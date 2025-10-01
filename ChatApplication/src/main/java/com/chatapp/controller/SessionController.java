package com.chatapp.controller;

import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    @PostMapping("/username")
    public void setUsername(@RequestBody Map<String, String> body, HttpSession session) {
        String username = body.get("username");
        session.setAttribute("username", username);
        System.out.println("Stored username in HTTP session: " + username);
    }
}