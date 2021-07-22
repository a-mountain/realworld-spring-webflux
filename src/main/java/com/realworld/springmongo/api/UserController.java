package com.realworld.springmongo.api;

import com.realworld.springmongo.security.TokenPrincipal;
import com.realworld.springmongo.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/users/login")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserWithToken> login(@RequestBody @Valid UserAuthenticationRequest request) {
        return userService.login(request);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserWithToken> signup(@RequestBody @Valid UserRegistrationRequest request) {
        return userService.signup(request);
    }

    @GetMapping("/user")
    public Mono<UserWithToken> currentUser(@AuthenticationPrincipal Mono<TokenPrincipal> principalMono) {
        return userService.getCurrentUser(principalMono);
    }

    @PutMapping("/user")
    public Mono<UserWithToken> updateUser(@RequestBody @Valid UpdateUserRequest request, @AuthenticationPrincipal Mono<TokenPrincipal> principal) {
        return userService.updateUser(request, principal);
    }

    @GetMapping("/profiles/{username}")
    public Mono<ProfileDto> getProfile(@PathVariable String username, @AuthenticationPrincipal Mono<TokenPrincipal> principalMono) {
        if (principalMono == null) {
            return userService.getProfile(username, "");
        }
        return principalMono.flatMap(principal -> userService.getProfile(username, principal.userId()));
    }

    @PostMapping("/profiles/{username}/follow")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ProfileDto> follow(@PathVariable String username, @AuthenticationPrincipal Mono<TokenPrincipal> tokenPrincipal) {
        return tokenPrincipal.flatMap(principal -> userService.follow(username, principal.userId()));
    }

    @DeleteMapping("/profiles/{username}/follow")
    public Mono<ProfileDto> unfollow(@PathVariable String username, @AuthenticationPrincipal Mono<TokenPrincipal> tokenPrincipal) {
        return tokenPrincipal.flatMap(principal -> userService.unfollow(username, principal.userId()));
    }
}
