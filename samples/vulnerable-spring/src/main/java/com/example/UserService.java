package com.example;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    public User create(CreateUserRequest request) {
        return new User();
    }
}
