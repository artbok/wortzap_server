package org.artbok.translator.controller;

import lombok.RequiredArgsConstructor;
import org.artbok.translator.model.User;
import org.artbok.translator.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    @PostMapping
    public void addNewUser(
            @RequestBody User user) {
        userService.insertUser(user);
    }
}
