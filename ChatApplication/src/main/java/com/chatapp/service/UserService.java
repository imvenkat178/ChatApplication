package com.chatapp.service;

import com.chatapp.dto.ChatMessageDTO;
import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private final AmqpAdmin amqpAdmin;
    private final DirectExchange chatExchange;
    private final RabbitTemplate rabbitTemplate;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SimpMessagingTemplate messagingTemplate; // WebSocket sender
    private final ObjectMapper objectMapper;
    private final ConnectionFactory connectionFactory;

    private final Map<String, SimpleMessageListenerContainer> activeListeners = new ConcurrentHashMap<>();

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AmqpAdmin amqpAdmin,
                       DirectExchange chatExchange,
                       RabbitTemplate rabbitTemplate,
                       SimpMessagingTemplate messagingTemplate,
                       ObjectMapper objectMapper,
                       ConnectionFactory connectionFactory) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.amqpAdmin = amqpAdmin;
        this.chatExchange = chatExchange;
        this.rabbitTemplate = rabbitTemplate;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
        this.connectionFactory = connectionFactory;
    }

    public void register(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        // Create queue and listener
        createUserQueueAndListener(user.getUsername());
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        return user;
    }

    // Create RabbitMQ queue and listener that pushes to WebSocket
    private void createUserQueueAndListener(String username) {
        String queueName = username + ".queue";

        Queue queue = new Queue(queueName, true);
        amqpAdmin.declareQueue(queue);

        Binding binding = BindingBuilder.bind(queue).to(chatExchange).with(username);
        amqpAdmin.declareBinding(binding);

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName);
        container.setMessageListener((Message message) -> {
            try {
                String body = new String(message.getBody());
                System.out.println("RabbitMQ -> " + username + ": " + body);

                ChatMessageDTO chatMessage = objectMapper.readValue(body, ChatMessageDTO.class);

                // CHANGED: Send to a regular topic instead of user-specific
                messagingTemplate.convertAndSend(
                        "/topic/messages." + username,  // Topic specific to this user
                        chatMessage
                );

                System.out.println("Pushed to WebSocket: " + username);

            } catch (Exception e) {
                System.err.println("Error processing message: " + e.getMessage());
                e.printStackTrace();
            }
        });
        container.start();

        activeListeners.put(username, container);
        System.out.println("Listener created for: " + username);
    }

    public void startListenerForUser(String username) {
        if (!activeListeners.containsKey(username)) {
            createUserQueueAndListener(username);
        }
    }

    public void stopListenerForUser(String username) {
        SimpleMessageListenerContainer container = activeListeners.get(username);
        if (container != null) {
            container.stop();
            activeListeners.remove(username);
        }
    }
}