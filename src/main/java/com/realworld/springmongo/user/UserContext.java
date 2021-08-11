package com.realworld.springmongo.user;

import reactor.core.publisher.Mono;

public interface UserContext {
    Mono<User> getCurrentUser();

    Mono<UserAndToken> getCurrentUserAndToken();

    record UserAndToken(User user, String token) {
    }
}
