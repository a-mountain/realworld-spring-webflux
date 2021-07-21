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
}
