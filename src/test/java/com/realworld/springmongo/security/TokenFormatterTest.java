package com.realworld.springmongo.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenFormatterTest {

    private final TokenFormatter formatter = new TokenFormatter();

    @Test
    void shouldGetRowToken() {
        var header = "Token 1234";
        var token = formatter.getRowToken(header);
        assertThat(token).isEqualTo("1234");
    }

    @Test
    void shouldFormatToken() {
        var rowToken = "1234";
        var formattedToken = formatter.formatToken(rowToken);
        assertThat(formattedToken).isEqualTo("Token 1234");
    }
}