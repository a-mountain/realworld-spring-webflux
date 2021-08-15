package com.realworld.springmongo.api;

import com.realworld.springmongo.api.wrappers.ProfileWrapper;
import com.realworld.springmongo.api.wrappers.UserWrapper.UpdateUserRequestWrapper;
import com.realworld.springmongo.api.wrappers.UserWrapper.UserAuthenticationRequestWrapper;
import com.realworld.springmongo.api.wrappers.UserWrapper.UserRegistrationRequestWrapper;
import com.realworld.springmongo.api.wrappers.UserWrapper.UserViewWrapper;
import com.realworld.springmongo.user.UserContextProvider;
import com.realworld.springmongo.user.UserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserFacade userFacade;
    private final UserContextProvider userContextProvider;

    @PostMapping("/users/login")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserViewWrapper> login(@RequestBody @Valid UserAuthenticationRequestWrapper request) {
        return userFacade.login(request.getContent()).map(UserViewWrapper::new);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserViewWrapper> signup(@RequestBody @Valid UserRegistrationRequestWrapper request) {
        return userFacade.signup(request.getContent()).map(UserViewWrapper::new);
    }

    @GetMapping("/user")
    public Mono<UserViewWrapper> currentUser() {
        return userFacade.getCurrentUser().map(UserViewWrapper::new);
    }

    @PutMapping("/user")
    public Mono<UserViewWrapper> updateUser(@RequestBody @Valid UpdateUserRequestWrapper request) {
        return userContextProvider.getCurrentUserContext()
                .flatMap(it -> userFacade.updateUser(request.getContent(), it)).map(UserViewWrapper::new);
    }

    @GetMapping("/profiles/{username}")
    public Mono<ProfileWrapper> getProfile(@PathVariable String username) {
        return userContextProvider.getCurrentUserOrEmpty()
                .flatMap((currentUser -> userFacade.getProfile(username, currentUser)))
                .switchIfEmpty(userFacade.getProfile(username)).map(ProfileWrapper::new);
    }

    @PostMapping("/profiles/{username}/follow")
    public Mono<ProfileWrapper> follow(@PathVariable String username) {
        return userContextProvider.getCurrentUserOrEmpty()
                .flatMap(currentUser -> userFacade.follow(username, currentUser)).map(ProfileWrapper::new);
    }

    @DeleteMapping("/profiles/{username}/follow")
    public Mono<ProfileWrapper> unfollow(@PathVariable String username) {
        return userContextProvider.getCurrentUserOrEmpty()
                .flatMap(currentUser -> userFacade.unfollow(username, currentUser)).map(ProfileWrapper::new);
    }
}
