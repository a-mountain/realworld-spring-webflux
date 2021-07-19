package com.realworld.springmongo.api;

import com.realworld.springmongo.user.UserAuthenticationRequest;
import com.realworld.springmongo.user.UserAuthenticationResponse;
import com.realworld.springmongo.user.UserRegistrationRequest;
import com.realworld.springmongo.user.UserRepository;
import helpers.user.UserSamples;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserApiIT {

    @Autowired
    WebTestClient client;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll().block();
    }

    @Test
    void shouldSignupUser() {
        var userRegistrationRequest = UserSamples.sampleUserRegistrationRequest();
        var result = signup(userRegistrationRequest)
                .expectStatus()
                .isCreated()
                .expectBody(UserAuthenticationResponse.class)
                .returnResult();
        var body = Objects.requireNonNull(result.getResponseBody());
        assertThat(body.getUsername()).isEqualTo(userRegistrationRequest.getUsername());
        assertThat(body.getEmail()).isEqualTo(userRegistrationRequest.getEmail());
        assertThat(body.getBio()).isEmpty();
        assertThat(body.getImage()).isNull();
        assertThat(body.getToken()).contains("Token ");
    }

    @Test
    void shouldLoginRegisteredUser() {
        var userRegistrationRequest = UserSamples.sampleUserRegistrationRequest();
        signup(userRegistrationRequest);
        var userAuthenticationRequest = UserSamples.sampleUserAuthenticationRequest();
        var result = login(userAuthenticationRequest)
                .expectStatus()
                .isCreated()
                .expectBody(UserAuthenticationResponse.class)
                .returnResult()
                .getResponseBody();
        Objects.requireNonNull(result);
        assertThat(result.getUsername()).isEqualTo(userRegistrationRequest.getUsername());
        assertThat(result.getEmail()).isEqualTo(userRegistrationRequest.getEmail());
        assertThat(result.getBio()).isEmpty();
        assertThat(result.getImage()).isNull();
        assertThat(result.getToken()).contains("Token ");
    }

    private WebTestClient.ResponseSpec signup(UserRegistrationRequest userRegistrationRequest) {
        return client.post()
                .uri("/api/users")
                .bodyValue(userRegistrationRequest)
                .exchange();
    }


    private WebTestClient.ResponseSpec login(UserAuthenticationRequest userAuthenticationRequest) {
        return client.post()
                .uri("/api/users/login")
                .bodyValue(userAuthenticationRequest)
                .exchange();
    }
}