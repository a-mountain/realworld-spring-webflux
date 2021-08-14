package com.realworld.springmongo.api;

import com.realworld.springmongo.api.wrappers.ProfileWrapper;
import com.realworld.springmongo.api.wrappers.UserWrapper.UpdateUserRequestWrapper;
import com.realworld.springmongo.api.wrappers.UserWrapper.UserAuthenticationRequestWrapper;
import com.realworld.springmongo.api.wrappers.UserWrapper.UserRegistrationRequestWrapper;
import com.realworld.springmongo.api.wrappers.UserWrapper.UserViewWrapper;
import com.realworld.springmongo.user.UserContext;
import com.realworld.springmongo.user.UserService;
import com.realworld.springmongo.user.dto.ProfileView;
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
    public Mono<UserViewWrapper> login(@RequestBody @Valid UserAuthenticationRequestWrapper request) {
        return userService.login(request.getContent()).map(UserViewWrapper::new);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserViewWrapper> signup(@RequestBody @Valid UserRegistrationRequestWrapper request) {
        return userService.signup(request.getContent()).map(UserViewWrapper::new);
    }

    @GetMapping("/user")
    public Mono<UserViewWrapper> currentUser() {
        return userService.getCurrentUser().map(UserViewWrapper::new);
    }

    @PutMapping("/user")
    public Mono<UserViewWrapper> updateUser(@RequestBody @Valid UpdateUserRequestWrapper request) {
        return userContext.getCurrentUserAndToken()
                .flatMap(it -> userService.updateUser(request.getContent(), it)).map(UserViewWrapper::new);
    }

    @GetMapping("/profiles/{username}")
    public Mono<ProfileWrapper> getProfile(@PathVariable String username) {
        return userContext.getCurrentUserOrEmpty()
                .flatMap((currentUser -> userService.getProfile(username, currentUser)))
                .switchIfEmpty(userService.getProfile(username)).map(ProfileWrapper::new);
    }

    @PostMapping("/profiles/{username}/follow")
    public Mono<ProfileWrapper> follow(@PathVariable String username) {
        return userContext.getCurrentUserOrEmpty()
                .flatMap(currentUser -> userService.follow(username, currentUser)).map(ProfileWrapper::new);
    }

    @DeleteMapping("/profiles/{username}/follow")
    public Mono<ProfileWrapper> unfollow(@PathVariable String username) {
        return userContext.getCurrentUserOrEmpty()
                .flatMap(currentUser -> userService.unfollow(username, currentUser)).map(ProfileWrapper::new);
    }
}
