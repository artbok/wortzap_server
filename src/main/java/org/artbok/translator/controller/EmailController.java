package org.artbok.translator.controller;

import java.time.Duration;
import java.util.Objects;
import java.util.Random;

import lombok.RequiredArgsConstructor;
import org.artbok.translator.model.User;
import org.artbok.translator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping("auth")
public class EmailController {
    @Autowired
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @RequestMapping("/get-email-code")
    @PostMapping
    public void getEmailCode(String email) {
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
        String code = String.valueOf(random.nextInt(999999));
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

    @RequestMapping("/enter-email-code")
    @PostMapping
    public String enterCode(String email, String code) {
        User user = userRepository.findByEmail(email);
        Instant nowUtc = Instant.now();
        OffsetDateTime nowOffsetUtc = OffsetDateTime.now(ZoneOffset.UTC);
        Duration duration = Duration.between(nowOffsetUtc, user.requestDate);
        long differenceInMinutes = Math.abs(duration.toMinutes());
        if (differenceInMinutes < 2 && Objects.equals(code, user.tempCode)) {
            user.password = UUID.randomUUID().toString().replace("-", "");
            userRepository.save(user);
            return user.password;
        }
        return "Wrong code";
    }
}
