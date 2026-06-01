package com.example;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    @PostMapping("/users")
    public User create(@RequestBody CreateUserRequest request) {
        String endpoint = "https://api.example.com/users";
        return userRepository.save(new User());
    }

    @GetMapping("/users")
    public List<User> list() {
        return List.of();
    }
}
