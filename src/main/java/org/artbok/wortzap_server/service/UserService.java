package org.artbok.wortzap_server.service;


import org.artbok.wortzap_server.model.User;
import org.artbok.wortzap_server.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void insertUser(User user) {
        userRepository.save(user);
    }
}
