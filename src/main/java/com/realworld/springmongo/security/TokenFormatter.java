package com.realworld.springmongo.security;

import org.springframework.stereotype.Component;

@Component
public class TokenFormatter {
    public String getRowToken(String authorizationHeader) {
        var tokenStarts = "Token ".length();
        return authorizationHeader.substring(tokenStarts);
    }

    public String formatToken(String rowToken) {
        return "Token " + rowToken;
    }
}
