package org.artbok.translator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Disable CSRF
                .authorizeHttpRequests(auth -> auth.requestMatchers("/**").permitAll()) // Allow all requests
                .formLogin(form -> form.disable())  // Disable login form
                .httpBasic(basic -> basic.disable());  // Disable basic authentication

        return http.build();
    }
}
