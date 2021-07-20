package com.realworld.springmongo.security;

import com.realworld.springmongo.exceptions.InvalidRequestException;
import org.springframework.stereotype.Component;

@Component
public class TokenFormatter {
    public String getRowToken(String authorizationHeader) {
        if (!authorizationHeader.startsWith("Token ")) {
            throw new InvalidRequestException("Authorization Header", "has no `Token` prefix");
        }
        var tokenStarts = "Token ".length();
        return authorizationHeader.substring(tokenStarts);
    }

    public String formatToken(String rowToken) {
        return "Token " + rowToken;
    }
}
