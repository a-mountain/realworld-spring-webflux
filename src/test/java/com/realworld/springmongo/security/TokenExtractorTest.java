package com.realworld.springmongo.security;

import com.realworld.springmongo.exceptions.InvalidRequestException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class TokenExtractorTest {

    private final TokenExtractor formatter = new TokenExtractor();

    @Test
    void shouldThrowError() {
        var token = "1234";
        var throwable = catchThrowable(() -> formatter.extractToken(token));
        assertThat(throwable)
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Authorization Header: has no `Token` prefix");
    }

    @Test
    void shouldGetRowToken() {
        var header = "Token 1234";
        var token = formatter.extractToken(header);
        assertThat(token).isEqualTo("1234");
    }
}