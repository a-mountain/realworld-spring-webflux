package com.realworld.springmongo.api;

import com.realworld.springmongo.user.UserContext;
import com.realworld.springmongo.user.UserService;
import com.realworld.springmongo.user.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserContext userContext;

    @PostMapping("/users/login")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserView> login(@RequestBody @Valid UserAuthenticationRequest request) {
        return userService.login(request);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserView> signup(@RequestBody @Valid UserRegistrationRequest request) {
        return userService.signup(request);
    }

    @GetMapping("/user")
    public Mono<UserView> currentUser() {
        return userService.getCurrentUser();
    }

    @PutMapping("/user")
    public Mono<UserView> updateUser(@RequestBody @Valid UpdateUserRequest request) {
        return userContext.getCurrentUserAndToken()
                .flatMap(it -> userService.updateUser(request, it));
    }

    @GetMapping("/profiles/{username}")
    public Mono<ProfileView> getProfile(@PathVariable String username) {
        return userContext.getCurrentUserOrEmpty()
                .flatMap((currentUser -> userService.getProfile(username, currentUser)))
                .switchIfEmpty(userService.getProfile(username));
    }

    @PostMapping("/profiles/{username}/follow")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ProfileView> follow(@PathVariable String username) {
        return userContext.getCurrentUserOrEmpty()
                .flatMap(currentUser -> userService.follow(username, currentUser));
    }

    @DeleteMapping("/profiles/{username}/follow")
    public Mono<ProfileView> unfollow(@PathVariable String username) {
        return userContext.getCurrentUserOrEmpty()
                .flatMap(currentUser -> userService.unfollow(username, currentUser));
    }
}
