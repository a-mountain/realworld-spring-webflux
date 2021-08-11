package helpers.user;

import com.realworld.springmongo.user.dto.*;
import helpers.TokenHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

public class UserApiSupport {
    private final WebTestClient client;

    public UserApiSupport(WebTestClient client) {
        this.client = client;
    }

    public EntityExchangeResult<UserView> updateUser(String token, UpdateUserRequest updateUserRequest) {
        return updateUser(token, updateUserRequest, UserView.class);
    }

    public <T> EntityExchangeResult<T> updateUser(String token, UpdateUserRequest updateUserRequest, Class<T> bodyType) {
        return client.put()
                .uri("/api/user")
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(token))
                .bodyValue(updateUserRequest)
                .exchange()
                .expectBody(bodyType)
                .returnResult();
    }

    public EntityExchangeResult<UserView> currentUser(String token) {
        return client.get()
                .uri("/api/user")
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(token))
                .exchange()
                .expectBody(UserView.class)
                .returnResult();
    }

    public EntityExchangeResult<UserView> signup(UserRegistrationRequest userRegistrationRequest) {
        return client.post()
                .uri("/api/users")
                .bodyValue(userRegistrationRequest)
                .exchange()
                .expectBody(UserView.class)
                .returnResult();
    }

    public UserView signup() {
        var user = signup(UserSamples.sampleUserRegistrationRequest()).getResponseBody();
        assert user != null;
        return user;
    }

    public EntityExchangeResult<UserView> login(UserAuthenticationRequest userAuthenticationRequest) {
        return client.post()
                .uri("/api/users/login")
                .bodyValue(userAuthenticationRequest)
                .exchange()
                .expectBody(UserView.class)
                .returnResult();
    }

    public EntityExchangeResult<ProfileView> getProfile(String username) {
        return client.get()
                .uri("/api/profiles/" + username)
                .exchange()
                .expectBody(ProfileView.class)
                .returnResult();
    }

    public EntityExchangeResult<ProfileView> getProfile(String username, String token) {
        return client.get()
                .uri("/api/profiles/" + username)
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(token))
                .exchange()
                .expectBody(ProfileView.class)
                .returnResult();
    }

    public EntityExchangeResult<ProfileView> follow(String username, String token) {
        return client.post()
                .uri("/api/profiles/" + username + "/follow")
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(token))
                .exchange()
                .expectBody(ProfileView.class)
                .returnResult();
    }

    public EntityExchangeResult<ProfileView> unfollow(String followeeUsername, String authToken) {
        return client.delete()
                .uri("/api/profiles/" + followeeUsername + "/follow")
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken))
                .exchange()
                .expectBody(ProfileView.class)
                .returnResult();
    }
}
