package helpers.user;

import com.realworld.springmongo.api.wrappers.ProfileWrapper;
import com.realworld.springmongo.api.wrappers.UserWrapper.UpdateUserRequestWrapper;
import com.realworld.springmongo.api.wrappers.UserWrapper.UserAuthenticationRequestWrapper;
import com.realworld.springmongo.api.wrappers.UserWrapper.UserRegistrationRequestWrapper;
import com.realworld.springmongo.api.wrappers.UserWrapper.UserViewWrapper;
import com.realworld.springmongo.user.dto.*;
import helpers.TokenHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

public class UserApiSupport {
    private final WebTestClient client;

    public UserApiSupport(WebTestClient client) {
        this.client = client;
    }

    public UserView updateUser(String token, UpdateUserRequest updateUserRequest) {
        var result = client.put()
                .uri("/api/user")
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(token))
                .bodyValue(new UpdateUserRequestWrapper(updateUserRequest))
                .exchange()
                .expectBody(UserViewWrapper.class)
                .returnResult();
        return result.getResponseBody().getContent();
    }

    public UserView currentUser(String token) {
        var response = client.get()
                .uri("/api/user")
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(token))
                .exchange()
                .expectBody(UserViewWrapper.class)
                .returnResult();
        return response.getResponseBody().getContent();
    }

    public UserView signup(UserRegistrationRequest userRegistrationRequest) {
        var response = client.post()
                .uri("/api/users")
                .bodyValue(new UserRegistrationRequestWrapper(userRegistrationRequest))
                .exchange()
                .expectBody(UserViewWrapper.class)
                .returnResult();
        return response.getResponseBody().getContent();
    }

    public UserView signup() {
        var user = signup(UserSamples.sampleUserRegistrationRequest());
        assert user != null;
        return user;
    }

    public UserView login(UserAuthenticationRequest userAuthenticationRequest) {
        var response = client.post()
                .uri("/api/users/login")
                .bodyValue(new UserAuthenticationRequestWrapper(userAuthenticationRequest))
                .exchange()
                .expectBody(UserViewWrapper.class)
                .returnResult();
        return response.getResponseBody().getContent();
    }

    public ProfileView getProfile(String username) {
        var result = client.get()
                .uri("/api/profiles/" + username)
                .exchange()
                .expectBody(ProfileWrapper.class)
                .returnResult();
        return result.getResponseBody().getContent();
    }

    public ProfileView getProfile(String username, String token) {
        var result = client.get()
                .uri("/api/profiles/" + username)
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(token))
                .exchange()
                .expectBody(ProfileWrapper.class)
                .returnResult();
        return result.getResponseBody().getContent();
    }

    public ProfileView follow(String username, String token) {
        var result = client.post()
                .uri("/api/profiles/" + username + "/follow")
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(token))
                .exchange()
                .expectBody(ProfileWrapper.class)
                .returnResult();
        return result.getResponseBody().getContent();
    }

    public ProfileView unfollow(String followeeUsername, String authToken) {
        var result = client.delete()
                .uri("/api/profiles/" + followeeUsername + "/follow")
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken))
                .exchange()
                .expectBody(ProfileWrapper.class)
                .returnResult();
        return result.getResponseBody().getContent();
    }
}
