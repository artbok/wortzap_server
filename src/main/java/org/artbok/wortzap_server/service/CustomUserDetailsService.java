package org.artbok.wortzap_server.service;

import java.util.Set;

import org.artbok.wortzap_server.model.User;
import org.artbok.wortzap_server.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null || !user.verified) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        Set<SimpleGrantedAuthority> authorities = Set.of(new SimpleGrantedAuthority("USER")); // You might fetch roles from the database later

        return new org.springframework.security.core.userdetails.User(user.email, user.password, authorities);
    }
}