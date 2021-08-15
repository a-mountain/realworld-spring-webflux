package com.realworld.springmongo.user;

import com.realworld.springmongo.user.UserContextProvider.UserContext;
import com.realworld.springmongo.user.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserFacade {

    private final CredentialsService credentialsService;
    private final UserRepository userRepository;
    private final UserUpdater userUpdater;
    private final UserContextProvider userContextProvider;

    public Mono<ProfileView> getProfile(String profileUsername, User viewer) {
        return userRepository.findByUsernameOrError(profileUsername)
                .map(user -> ProfileView.toProfileViewForViewer(user, viewer));
    }

    public Mono<ProfileView> getProfile(String profileUsername) {
        return userRepository.findByUsernameOrError(profileUsername)
                .map(user -> ProfileView.toProfileView(user, false));
    }

    public Mono<UserView> signup(UserRegistrationRequest request) {
        return credentialsService.signup(request);
    }

    public Mono<UserView> login(UserAuthenticationRequest request) {
        return credentialsService.login(request);
    }

    public Mono<UserView> getCurrentUser() {
        return userContextProvider.getCurrentUserContext()
                .map(UserView::fromUserAndToken);
    }

    public Mono<UserView> updateUser(UpdateUserRequest request, UserContext userContext) {
        var user = userContext.user();
        var token = userContext.token();
        return userUpdater.updateUser(request, user)
                .flatMap(userRepository::save)
                .map(it -> UserView.fromUserAndToken(it, token));
    }

    public Mono<ProfileView> follow(String username, User follower) {
        return userRepository.findByUsernameOrError(username)
                .flatMap(userToFollow -> {
                    follower.follow(userToFollow);
                    return userRepository.save(follower).thenReturn(userToFollow);
                })
                .map(ProfileView::toFollowedProfileViewOf);
    }

    public Mono<ProfileView> unfollow(String username, User follower) {
        return userRepository.findByUsernameOrError(username)
                .flatMap(userToUnfollow -> {
                    follower.unfollow(userToUnfollow);
                    return userRepository.save(follower).thenReturn(userToUnfollow);
                })
                .map(ProfileView::toUnfollowedProfileView);
    }
}
