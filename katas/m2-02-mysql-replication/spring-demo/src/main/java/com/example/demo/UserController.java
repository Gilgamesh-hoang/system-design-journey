package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;

    // GHI D? LI?U (Proxy s? t? di?u hu?ng query này t?i Primary)
    @PostMapping("/write")
    public User write(@RequestParam String name) {
        User user = new User();
        user.setName(name);
        User savedUser = userRepository.save(user);
        logger.info("Wrote user ID: " + savedUser.getId() + " to Primary via Proxy");
        return savedUser;
    }

    // Ð?C D? LI?U (Proxy s? t? di?u hu?ng query này t?i Replica)
    @GetMapping("/read/{id}")
    public String read(@PathVariable Long id) {
        logger.info("Reading user ID: " + id + " via Proxy...");
        Optional<User> userOpt = userRepository.findById(id);
        
        if (userOpt.isPresent()) {
            return "SUCCESS: Found " + userOpt.get().getName();
        } else {
            return "STALE READ DETECTED: User not found yet!";
        }
    }
}
