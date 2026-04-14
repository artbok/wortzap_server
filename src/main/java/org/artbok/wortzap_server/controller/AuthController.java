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
import org.artbok.wortzap_server.dto.AuthResponse;
import org.artbok.wortzap_server.service.CustomUserDetailsService;
import org.artbok.wortzap_server.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;


@RestController
@RequiredArgsConstructor
@RequestMapping("auth")
public class AuthController {
    @Autowired
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final TempCodeRepository tempCodeRepository;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<?> auth(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String password = requestBody.get("password");

        User existingUser = userRepository.findByEmail(email);

        if (existingUser != null && BCrypt.checkpw(password, existingUser.password)) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            String accessToken = jwtService.generateAccessToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("WRONG_CREDENTIALS");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> requestBody) {
        String refreshToken = requestBody.get("refreshToken");

        try {
            String userEmail = jwtService.extractUsername(refreshToken);

            if (userEmail != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(refreshToken, userDetails)) {
                    // Генерируем новую пару токенов
                    String newAccessToken = jwtService.generateAccessToken(userDetails);
                    String newRefreshToken = jwtService.generateRefreshToken(userDetails);

                    return ResponseEntity.ok(new AuthResponse(newAccessToken, newRefreshToken));
                }
            }
        } catch (Exception e) {
            // Токен невалидный или протух
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("INVALID_REFRESH_TOKEN");
    }


    @PostMapping("/send-login-code")
    public ResponseEntity<?> sendLoginCode(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        User user = userRepository.findByEmail(email);

        // Если пользователя нет в базе, мы не можем его авторизовать
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("USER_NOT_FOUND");
        }

        String code;
        OffsetDateTime sevenMinutesAgo = OffsetDateTime.now().minusMinutes(7);
        Optional<TempCode> tmpCode = tempCodeRepository.findByEmailAndRequestDateAfter(email, sevenMinutesAgo);

        // Генерируем новый код или берем существующий, если он еще не протух
        if (tmpCode.isEmpty()) {
            Random random = new Random();
            code = String.valueOf(random.nextInt(899999) + 100000);
            TempCode tempCode = new TempCode(email, code, OffsetDateTime.now(ZoneOffset.UTC));
            tempCodeRepository.save(tempCode);
        } else {
            code = tmpCode.get().code;
        }

        // Отправка письма
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("help.sprechai@gmail.com");
            msg.setTo(email);
            msg.setSubject("Your WortZap Login Code");
            msg.setText("You requested a login code for WortZap.\n\n" +
                    code +
                    "\n\nIf you did not request this code, you can safely ignore this email.");
            mailSender.send(msg);
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("EMAIL_SEND_ERROR");
        }

        return ResponseEntity.ok("SUCCESS");
    }

    @PostMapping("/login-with-code")
    public ResponseEntity<?> loginWithCode(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String code = requestBody.get("code");

        OffsetDateTime sevenMinutesAgo = OffsetDateTime.now().minusMinutes(7);
        Optional<TempCode> tempCode = tempCodeRepository.findByEmailAndCodeAndRequestDateAfter(email, code, sevenMinutesAgo);

        if (tempCode.isPresent()) {
            User existingUser = userRepository.findByEmail(email);
            if (existingUser != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                String accessToken = jwtService.generateAccessToken(userDetails);
                String refreshToken = jwtService.generateRefreshToken(userDetails);

                tempCodeRepository.delete(tempCode.get());

                return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("WRONG_CODE");
    }



    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String password = requestBody.get("password");
        String code = requestBody.get("code");
        OffsetDateTime sevenMinutesAgo = OffsetDateTime.now().minusMinutes(7);
        Optional<TempCode> tempCode = tempCodeRepository.findByEmailAndCodeAndRequestDateAfter(email, code, sevenMinutesAgo);
        if (tempCode.isPresent()) {
            String encodedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            String nativeLanguage = requestBody.get("nativeLanguage");
            User existingUser = userRepository.findByEmail(email);
            if (existingUser != null) {
                return "USER_ALREADY_EXISTS";
            } else {
                User newUser = new User(email, encodedPassword, nativeLanguage);
                userRepository.save(newUser);
                return "SUCCESS";
            }
        }
        return "WRONG_CODE";
    }
}
