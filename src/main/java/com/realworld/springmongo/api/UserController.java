package com.realworld.springmongo.api;

import com.realworld.springmongo.security.TokenPrincipal;
import com.realworld.springmongo.user.UserAuthenticationRequest;
import com.realworld.springmongo.user.UserAuthenticationResponse;
import com.realworld.springmongo.user.UserRegistrationRequest;
import com.realworld.springmongo.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/users/login")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserAuthenticationResponse> login(@RequestBody UserAuthenticationRequest request) {
        return userService.login(request);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserAuthenticationResponse> signup(@RequestBody UserRegistrationRequest request) {
        return userService.signup(request);
    }

    @GetMapping("/user")
    public Mono<UserAuthenticationResponse> currentUser(@AuthenticationPrincipal Mono<TokenPrincipal> principalMono) {
        return userService.getCurrentUser(principalMono);
    }
}
