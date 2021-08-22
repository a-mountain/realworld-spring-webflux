package com.realworld.springmongo.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordService {
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public String encodePassword(String rowPassword) {
        return encoder.encode(rowPassword);
    }

    public boolean matchesRowPasswordWithEncodedPassword(String rowPassword, String encodedPassword) {
        return encoder.matches(rowPassword, encodedPassword);
    }
}
