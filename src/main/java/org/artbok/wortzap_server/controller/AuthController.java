package org.artbok.wortzap_server.controller;


import java.util.*;
import lombok.RequiredArgsConstructor;
import org.artbok.wortzap_server.model.TempCode;
import org.artbok.wortzap_server.model.User;
import org.artbok.wortzap_server.repository.TempCodeRepository;
import org.artbok.wortzap_server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import org.mindrot.jbcrypt.BCrypt;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;


@RestController
@RequiredArgsConstructor
@RequestMapping("auth")
public class AuthController {
    @Autowired
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final TempCodeRepository tempCodeRepository;

    @PostMapping("/validate")
    public String validate(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String password = requestBody.get("password");
        User existingUser = userRepository.findByEmail(email);
        if (existingUser != null) {
            if (BCrypt.checkpw(password, existingUser.password)) {
                if (existingUser.verified) {
                    return "verified";
                } else {
                    return "unverified";
                }
            }
        }
        return "wrongCredentials";
    }


    @RequestMapping("/register")
    @PostMapping
    public String register(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String password = requestBody.get("password");
        String encodedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String nativeLanguage = requestBody.get("nativeLanguage");
        User existingUser = userRepository.findByEmail(email);
        if (existingUser != null) {
            return "user with that email already exists";
        } else {
            User newUser = new User(email, encodedPassword, nativeLanguage);
            userRepository.save(newUser);
            sendEmailCode(requestBody);
            return "success";
        }
    }

    @RequestMapping("/send-email-code")
    @PostMapping
    public String sendEmailCode(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return "User not found";
        }
        Random random = new Random();
        String code = String.valueOf(random.nextInt(899999) + 100000);
        TempCode tempCode = new TempCode(email, code, OffsetDateTime.now(ZoneOffset.UTC));
        tempCodeRepository.save(tempCode);
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
            System.out.println(e);
        }
        return "Success";
    }

    @RequestMapping("/submit-email-code")
    @PostMapping
    public String submitCode(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String code = requestBody.get("code");
        User user = userRepository.findByEmail(email);
        OffsetDateTime sevenMinutesAgo = OffsetDateTime.now().minusMinutes(7);
        Optional<TempCode> tempCode = tempCodeRepository.findByEmailAndCodeAndRequestDateAfter(email, code, sevenMinutesAgo);
        if (tempCode.isPresent()) {
            user.verified = true;
            userRepository.save(user);
            return "success";
        }
        return "wrongCode";
    }
}
