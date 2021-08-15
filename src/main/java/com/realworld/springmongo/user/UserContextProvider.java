package com.realworld.springmongo.user;

import com.realworld.springmongo.security.TokenPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserContextProvider {

    private final UserRepository userRepository;

    public Mono<User> getCurrentUserOrEmpty() {
        return getCurrentUserContext()
                .map(UserContext::user);
    }

    public Mono<UserContext> getCurrentUserContext() {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(context -> {
                    var authentication = context.getAuthentication();
                    if (authentication == null) {
                        return Mono.empty();
                    }
                    var tokenPrincipal = (TokenPrincipal) authentication.getPrincipal();
                    return userRepository
                            .findById(tokenPrincipal.userId())
                            .map(user -> new UserContext(user, tokenPrincipal.token()));
                });
    }

    public record UserContext(User user, String token) {
    }
}
