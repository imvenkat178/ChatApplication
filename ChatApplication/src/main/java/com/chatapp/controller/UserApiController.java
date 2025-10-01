package com.chatapp.controller;

import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserApiController {

    private final UserRepository userRepository;

    public UserApiController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public List<String> getAllUsers(@RequestParam(required = false) String currentUser) {
        // Get currentUser from query parameter instead of Principal
        if (currentUser == null || currentUser.isEmpty()) {
            // Return all users if no currentUser specified
            return userRepository.findAll()
                    .stream()
                    .map(User::getUsername)
                    .collect(Collectors.toList());
        }

        // Exclude current user from list
        return userRepository.findAll()
                .stream()
                .map(User::getUsername)
                .filter(username -> !username.equals(currentUser))
                .collect(Collectors.toList());
    }
}