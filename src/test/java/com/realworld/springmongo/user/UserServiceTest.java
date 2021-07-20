package com.realworld.springmongo.user;

import com.realworld.springmongo.exceptions.InvalidRequestException;
import com.realworld.springmongo.security.JwtProperties;
import com.realworld.springmongo.security.JwtSigner;
import com.realworld.springmongo.security.TokenPrincipal;
import helpers.user.UserSamples;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class UserServiceTest {

    static UserService service;
    static PasswordService passwordService = new PasswordService();
    static UserRepository userRepository = Mockito.mock(UserRepository.class);

    @BeforeAll
    static void beforeAll() {
        var signer = new JwtSigner(new JwtProperties(86000));
        UserTokenProvider tokenProvider = signer::generateToken;
        service = new UserService(tokenProvider, passwordService, userRepository);
    }

    @Test
    void shouldThrowErrorWhenSignupWithDuplicateEmail() {
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(true));

        var userRegistrationRequest = UserSamples.sampleUserRegistrationRequest();
        var throwable = catchThrowable(() -> service.signup(userRegistrationRequest).block());

        assertThat(throwable)
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Email: already in use");
    }

    @Test
    void shouldThrowErrorWhenSignupWithDuplicateUsername() {
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
        when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(true));

        var userRegistrationRequest = UserSamples.sampleUserRegistrationRequest();
        var throwable = catchThrowable(() -> service.signup(userRegistrationRequest).block());

        assertThat(throwable)
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Username: already in use");
    }

    @Test
    void shouldThrowErrorWhenLoginWithUnregisteredUser() {
        when(userRepository.findByEmail(Mockito.any())).thenReturn(Mono.empty());

        var userAuthenticationRequest = UserSamples.sampleUserAuthenticationRequest();
        var throwable = catchThrowable(() -> service.login(userAuthenticationRequest).block());

        assertThat(throwable)
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Email: not found");
    }

    @Test
    void shouldThrowErrorWhenWrongPassword() {
        var user = UserSamples.sampleUser(passwordService).build();
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(user));

        var userAuthenticationRequest = UserSamples.sampleUserAuthenticationRequest()
                .setPassword("not default sample password");
        var throwable = catchThrowable(() -> service.login(userAuthenticationRequest).block());

        assertThat(throwable)
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Password: invalid");
    }

    @Test
    void shouldThrowErrorWhenUpdateUserWithDuplicateEmail() {
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(true));
        when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(false));
        when(userRepository.findById(anyString())).thenReturn(Mono.just(UserSamples.sampleUser(passwordService).build()));

        var updateUserRequest = UserSamples.sampleUpdateUserRequest();
        var throwable = catchThrowable(() -> service.updateUser(updateUserRequest, Mono.just(new TokenPrincipal("1", "Token"))).block());

        assertThat(throwable)
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Email: already in use");
    }

    @Test
    void shouldThrowErrorWhenUpdateUserWithDuplicateUsername() {
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
        when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(true));
        when(userRepository.findById(anyString())).thenReturn(Mono.just(UserSamples.sampleUser(passwordService).build()));

        var updateUserRequest = UserSamples.sampleUpdateUserRequest();
        var throwable = catchThrowable(() -> service.updateUser(updateUserRequest, Mono.just(new TokenPrincipal("1", "Token"))).block());

        assertThat(throwable)
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Username: already in use");
    }
}