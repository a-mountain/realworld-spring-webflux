package com.realworld.springmongo.api;

import com.realworld.springmongo.user.UpdateUserRequest;
import com.realworld.springmongo.user.UserRegistrationRequest;
import com.realworld.springmongo.user.UserRepository;
import com.realworld.springmongo.user.UserWithToken;
import helpers.user.UserApiSupport;
import helpers.user.UserSamples;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserApiTest {

    @Autowired
    WebTestClient client;

    @Autowired
    UserRepository userRepository;

    UserApiSupport api;

    @BeforeEach
    void setUp() {
        api = new UserApiSupport(client);
        userRepository.deleteAll().block();
    }

    @Test
    void shouldSignupUser() {
        var userRegistrationRequest = UserSamples.sampleUserRegistrationRequest();

        var result = api.signup(userRegistrationRequest);
        var body = requireNonNull(result.getResponseBody());

        assertThatSignupResponseIsValid(userRegistrationRequest, body);
    }

    private void assertThatSignupResponseIsValid(UserRegistrationRequest userRegistrationRequest, UserWithToken body) {
        assertThat(body.getUsername()).isEqualTo(userRegistrationRequest.getUsername());
        assertThat(body.getEmail()).isEqualTo(userRegistrationRequest.getEmail());
        assertThat(body.getBio()).isNull();
        assertThat(body.getImage()).isNull();
        assertThat(body.getToken()).isNotEmpty();
    }

    @Test
    void shouldLoginRegisteredUser() {
        var userRegistrationRequest = UserSamples.sampleUserRegistrationRequest();
        api.signup(userRegistrationRequest);

        var userAuthenticationRequest = UserSamples.sampleUserAuthenticationRequest();
        var result = api.login(userAuthenticationRequest).getResponseBody();

        requireNonNull(result);
        assertThatLoginResponseIsValid(userRegistrationRequest, result);
    }

    private void assertThatLoginResponseIsValid(UserRegistrationRequest userRegistrationRequest, UserWithToken result) {
        assertThat(result.getUsername()).isEqualTo(userRegistrationRequest.getUsername());
        assertThat(result.getEmail()).isEqualTo(userRegistrationRequest.getEmail());
        assertThat(result.getBio()).isNull();
        assertThat(result.getImage()).isNull();
        assertThat(result.getToken()).isNotEmpty();
    }

    @Test
    void shouldGetCurrentUser() {
        var response = api.signup(UserSamples.sampleUserRegistrationRequest()).getResponseBody();
        requireNonNull(response);

        var body = api.currentUser(response.getToken()).getResponseBody();

        requireNonNull(body);
        assertThat(body.getUsername()).isEqualTo(response.getUsername());
        assertThat(body.getEmail()).isEqualTo(response.getEmail());
    }

    @Test
    void shouldUpdateUser() {
        var responseBody = api.signup(UserSamples.sampleUserRegistrationRequest()).getResponseBody();
        requireNonNull(responseBody);

        var updateUserRequest = UserSamples.sampleUpdateUserRequest();
        var body = api.updateUser(responseBody.getToken(), updateUserRequest).getResponseBody();

        requireNonNull(body);
        assertThatUserIsSavedAfterUpdate(updateUserRequest);
        assertThatUpdateUserResponseIsValid(updateUserRequest, body);
    }

    private void assertThatUserIsSavedAfterUpdate(UpdateUserRequest updateUserRequest) {
        var user = requireNonNull(userRepository.findByEmail(updateUserRequest.getEmail()).block());
        assertThat(user.getUsername()).isEqualTo(updateUserRequest.getUsername());
        assertThat(user.getBio()).isEqualTo(updateUserRequest.getBio());
        assertThat(user.getEmail()).isEqualTo(updateUserRequest.getEmail());
        assertThat(user.getImage()).isEqualTo(updateUserRequest.getImage());
    }

    private void assertThatUpdateUserResponseIsValid(UpdateUserRequest updateUserRequest, UserWithToken body) {
        assertThat(body.getBio()).isEqualTo(updateUserRequest.getBio());
        assertThat(body.getImage()).isEqualTo(updateUserRequest.getImage());
        assertThat(body.getUsername()).isEqualTo(updateUserRequest.getUsername());
        assertThat(body.getEmail()).isEqualTo(updateUserRequest.getEmail());
    }
}