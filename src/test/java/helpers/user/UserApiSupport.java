package helpers.user;

import com.realworld.springmongo.user.*;
import helpers.TokenHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

public class UserApiSupport {
    private final WebTestClient client;

    public UserApiSupport(WebTestClient client) {
        this.client = client;
    }

    public EntityExchangeResult<UserWithToken> updateUser(String token, UpdateUserRequest updateUserRequest) {
        return updateUser(token, updateUserRequest, UserWithToken.class);
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

    public EntityExchangeResult<UserWithToken> currentUser(String token) {
        return client.get()
                .uri("/api/user")
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(token))
                .exchange()
                .expectBody(UserWithToken.class)
                .returnResult();
    }

    public EntityExchangeResult<UserWithToken> signup(UserRegistrationRequest userRegistrationRequest) {
        return client.post()
                .uri("/api/users")
                .bodyValue(userRegistrationRequest)
                .exchange()
                .expectBody(UserWithToken.class)
                .returnResult();
    }

    public EntityExchangeResult<UserWithToken> login(UserAuthenticationRequest userAuthenticationRequest) {
        return client.post()
                .uri("/api/users/login")
                .bodyValue(userAuthenticationRequest)
                .exchange()
                .expectBody(UserWithToken.class)
                .returnResult();
    }

    public EntityExchangeResult<ProfileDto> getProfile(String username) {
        return client.get()
                .uri("/api/profiles/" + username)
                .exchange()
                .expectBody(ProfileDto.class)
                .returnResult();
    }

    public EntityExchangeResult<ProfileDto> getProfile(String username, String token) {
        return client.get()
                .uri("/api/profiles/" + username)
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(token))
                .exchange()
                .expectBody(ProfileDto.class)
                .returnResult();
    }

    public EntityExchangeResult<ProfileDto> follow(String username, String token) {
        return client.post()
                .uri("/api/profiles/" + username + "/follow")
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(token))
                .exchange()
                .expectBody(ProfileDto.class)
                .returnResult();
    }

    public EntityExchangeResult<ProfileDto> unfollow(String followeeUsername, String authToken) {
        return client.delete()
                .uri("/api/profiles/" + followeeUsername + "/follow")
                .header(HttpHeaders.AUTHORIZATION, TokenHelper.formatToken(authToken))
                .exchange()
                .expectBody(ProfileDto.class)
                .returnResult();
    }
}
