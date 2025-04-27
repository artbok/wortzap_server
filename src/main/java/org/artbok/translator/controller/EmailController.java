package org.artbok.translator.controller;

import java.time.Duration;
import java.util.*;

import lombok.RequiredArgsConstructor;
import org.artbok.translator.model.User;
import org.artbok.translator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;


@RestController
@RequiredArgsConstructor
@RequestMapping("auth")
public class EmailController {
    @Autowired
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @RequestMapping("/send-email-code")
    @PostMapping
    public void sendEmailCode(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        User user;
        User existingUser = userRepository.findByEmail(email);
        if (existingUser  == null) {
            user = new User(email);
            userRepository.save(user);
        } else {
            user = existingUser;
        }
        Instant nowUtc = Instant.now();
        OffsetDateTime nowOffsetUtc = OffsetDateTime.now(ZoneOffset.UTC);
        Random random = new Random();
        String code = String.valueOf(random.nextInt(899999) + 100000);
        user.tempCode = code;
        user.requestDate = nowOffsetUtc;
        userRepository.save(user);

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("help.sprechai@gmail.com");
            msg.setTo(email);
            msg.setSubject("Your SprechAI Login Code");
            msg.setText("You've requested a login code for SprechAI. Please use the following code to access your account:\n\n" +
                     code +
                    "\n\nThis code is valid for a limited time. If you did not request this code, you can safely ignore this email.");
            mailSender.send(msg);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    @RequestMapping("/submit-email-code")
    @PostMapping
    public HashMap<String, String> submitCode(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String code = requestBody.get("code");
        User user = userRepository.findByEmail(email);
        Instant nowUtc = Instant.now();
        OffsetDateTime nowOffsetUtc = OffsetDateTime.now(ZoneOffset.UTC);
        Duration duration = Duration.between(nowOffsetUtc, user.requestDate);
        long differenceInMinutes = Math.abs(duration.toMinutes());
        HashMap<String, String> response = new HashMap<>();
        if (differenceInMinutes < 2 && Objects.equals(code, user.tempCode)) {
            user.password = UUID.randomUUID().toString().replace("-", "");
            userRepository.save(user);
            response.put("status", "ok");
            response.put("token", user.password);
        } else {
            response.put("status", "wrongCode");
            response.put("token", "");
        }

        return response;
    }
}
