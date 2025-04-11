package org.artbok.translator.controller;

import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailController {
    @Autowired
    private final JavaMailSender mailSender;

    public EmailController(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @RequestMapping("/send-email")
    @PostMapping
    public void sendEmail(String toEmail) {
        Random random = new Random();
        String code = String.valueOf(random.nextInt(899999) + 100000);
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("help.sprechai@gmail.com");
            msg.setTo(toEmail);
            msg.setSubject("Your SprechAI Login Code");
            msg.setText("You've requested a login code for SprechAI. Please use the following code to access your account:\n\n" +
                     code +
                    "\n\nThis code is valid for a limited time. If you did not request this code, you can safely ignore this email.");
            mailSender.send(msg);
        } catch (Exception e) {
            System.out.println(e.toString());
        }


    }
}
