package com.example;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@RestController
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @PostMapping("/users")
    public User create(@RequestBody CreateUserRequest request) {
        return new User();
    }
}
