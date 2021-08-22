package com.realworld.springmongo.security;

import com.realworld.springmongo.exceptions.InvalidRequestException;
import org.springframework.stereotype.Component;

@Component
public class TokenExtractor {
    public String extractToken(String authorizationHeader) {
        if (!authorizationHeader.startsWith("Token ")) {
            throw new InvalidRequestException("Authorization Header", "has no `Token` prefix");
        }
        var tokenStarts = "Token ".length();
        return authorizationHeader.substring(tokenStarts);
    }
}
